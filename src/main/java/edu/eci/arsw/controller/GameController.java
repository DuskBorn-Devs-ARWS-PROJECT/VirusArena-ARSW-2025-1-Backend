package edu.eci.arsw.controller;


import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import edu.eci.arsw.model.Game;
import edu.eci.arsw.model.player.*;
import edu.eci.arsw.model.dto.GameDTOs.*;
import edu.eci.arsw.repository.GameRepository;
import edu.eci.arsw.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


@Controller
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final GameRepository gameRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public GameController(GameRepository gameRepository,
                          SimpMessagingTemplate messagingTemplate,
                          UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @MessageMapping("/lobby/{gameCode}/join")
    public void handleJoin(
            @Payload PlayerJoinRequest joinRequest,
            @DestinationVariable String gameCode) {

        logger.info("Nueva solicitud de unión a partida recibida");
        Game game = gameRepository.findOrCreateGame(gameCode);
        logger.info("Jugador añadido exitosamente");
        if (game.getPlayerById(joinRequest.getPlayerId()).isPresent()) {
            logger.warn("Intento de unión duplicada detectada");
            return;
        }

        if (game.getPlayers().isEmpty()) {
            game.setHostPlayerId(joinRequest.getPlayerId());
            // Actualizar rol del usuario a ROLE_HOST
            userRepository.findByUsername(joinRequest.getPlayerName())
                    .ifPresent(user -> {
                        user.setRole("ROLE_HOST");
                        userRepository.save(user);
                        logger.info("Se asignó rol de host a un usuario");
                    });
        }

        Player player = createPlayerWithDistribution(
                joinRequest.getPlayerId(),
                joinRequest.getPlayerName(),
                game
        );

        game.addPlayer(player);
        logger.info("Nuevo jugador añadido al juego {}", gameCode);
        messagingTemplate.convertAndSend("/topic/lobby/" + gameCode, createLobbyUpdate(game));
    }

    @MessageMapping("/game/{gameCode}/action")
    public void handlePlayerAction(
            @Payload PlayerActionRequest actionRequest,
            @DestinationVariable String gameCode) {

        Game game = gameRepository.findGameByCode(gameCode);
        if (game == null || game.getState() != Game.GameState.IN_PROGRESS) return;

        game.getPlayerById(actionRequest.getPlayerId()).ifPresent(player -> {
            if (actionRequest.getAction() == PlayerAction.USE_POWERUP && player instanceof Survivor) {
                ((Survivor) player).usePowerUp();
            } else {
                game.processPlayerAction(actionRequest.getPlayerId(), actionRequest.getAction());
            }

            game.getMap().placePlayer(player);

            messagingTemplate.convertAndSend(
                    "/topic/game/" + gameCode + "/update",
                    new GameStateDTO(
                            game.getGameCode(),
                            game.getState().toString(),
                            game.getMap().getGridCopy(),
                            new ArrayList<>(game.getPlayers()),
                            game.getRemainingTimeMillis()
                    ));
        });
    }

    @MessageMapping("/lobby/{gameCode}/ready")
    public void handleReady(
            @Payload PlayerReadyRequest readyRequest,
            @DestinationVariable String gameCode) {

        logger.debug("Actualización de estado 'ready' recibida");
        Game game = gameRepository.findGameByCode(gameCode);
        if (game != null) {
            game.getPlayerById(readyRequest.getPlayerId())
                    .ifPresent(player -> {
                        player.setReady(readyRequest.isReady());
                        logger.debug("Estado 'ready' actualizado");                    });
            messagingTemplate.convertAndSend("/topic/lobby/" + gameCode, createLobbyUpdate(game));
        }
    }

    @MessageMapping("/lobby/{gameCode}/start")
    public void handleStartGame(
            @Payload StartGameRequest startRequest,
            @DestinationVariable String gameCode) {

        logger.info("Solicitud de inicio recibida");
        Game game = gameRepository.findGameByCode(gameCode);
        if (game == null) {
            logger.error("Intento de iniciar juego no existente");
            return;
        }

        if (!game.getHostPlayerId().equals(startRequest.getHostPlayerId())) {
            logger.warn("Intento de inicio por no-host");           return;
        }

        if (!game.canStart()) {
            logger.warn("Intento de inicio fallido: condiciones no cumplidas");
            messagingTemplate.convertAndSendToUser(
                    startRequest.getHostPlayerId(),
                    "/queue/errors",
                    "No se puede iniciar. Verifica que todos estén listos."
            );
            return;
        }

        game.startGame();

        messagingTemplate.convertAndSend("/topic/game/" + gameCode + "/start",
                new GameStateDTO(
                        game.getGameCode(),
                        game.getState().toString(),
                        game.getMap().getGridCopy(),
                        new ArrayList<>(game.getPlayers()),
                        game.getRemainingTimeMillis()
                ));

        logger.info("Partida iniciada exitosamente");
    }

    @MessageMapping("/game/{gameCode}/collect")
    public void handleCollectPowerUp(
            @Payload PowerUpCollectRequest collectRequest,
            @DestinationVariable String gameCode) {

        logger.debug("Recolección de power-up en: {},{}",
                collectRequest.getX(), collectRequest.getY());

        Game game = gameRepository.findGameByCode(gameCode);
        if (game != null) {
            game.collectPowerUp(collectRequest.getX(), collectRequest.getY(),
                    collectRequest.getPlayerId());
        }
    }

    private Player createPlayerWithDistribution(String id, String name, Game game) {
        int initialX = random.nextInt(30) + 1;
        int initialY = random.nextInt(30) + 1;

        List<Player> players = new ArrayList<>(game.getPlayers());
        long infectedCount = players.stream().filter(p -> p instanceof Infected).count();
        int totalPlayers = players.size();

        boolean shouldBeInfected;

        if (totalPlayers == 0) {
            shouldBeInfected = random.nextDouble() < 0.2;
        } else if (infectedCount == 0 && totalPlayers >= 3) {
            shouldBeInfected = true;
        } else {
            double targetRatio = 0.25;
            double currentRatio = (double) infectedCount / totalPlayers;

            if (currentRatio < targetRatio) {
                shouldBeInfected = random.nextDouble() < (targetRatio - currentRatio);
            } else {
                shouldBeInfected = random.nextDouble() < 0.1;
            }
        }

        return shouldBeInfected ?
                new Infected(id, initialX, initialY, name) :
                new Survivor(id, initialX, initialY, name);
    }

    private LobbyUpdate createLobbyUpdate(Game game) {
        List<Player> playerList = new ArrayList<>(game.getPlayers());
        System.out.println("[DEBUG] Host ID: " + game.getHostPlayerId());
        return new LobbyUpdate(playerList, game.getHostPlayerId(), game.getState().toString());
    }
}
