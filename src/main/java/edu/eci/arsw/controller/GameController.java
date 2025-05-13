package edu.eci.arsw.controller;

import edu.eci.arsw.model.dto.GameDTOs;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import edu.eci.arsw.model.Game;
import edu.eci.arsw.model.player.*;
import edu.eci.arsw.model.dto.GameDTOs.*;
import edu.eci.arsw.repository.GameRepository;
import edu.eci.arsw.service.GameNotificationService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class GameController {

    private final GameRepository gameRepository;
    private final GameNotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Random random = new Random();

    public GameController(GameRepository gameRepository,
                          GameNotificationService notificationService,
                          SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/lobby/{gameCode}/join")
    public void handleJoin(
            @Payload PlayerJoinRequest joinRequest,
            @DestinationVariable String gameCode) {

        System.out.println("Solicitud de unión - GameCode: " + gameCode +
                ", PlayerId: " + joinRequest.getPlayerId() +
                ", Name: " + joinRequest.getPlayerName());

        Game game = gameRepository.findOrCreateGame(gameCode);
        System.out.println("Juego encontrado/creado: " + game.getGameCode() +
                ", Jugadores actuales: " + game.getPlayers().size());

        if (game.getPlayerById(joinRequest.getPlayerId()).isPresent()) {
            System.out.println("⚠️ Jugador ya existe: " + joinRequest.getPlayerId());
            return;
        }

        if (game.getPlayers().isEmpty()) {
            game.setHostPlayerId(joinRequest.getPlayerId());
        }

        Player player = createPlayerWithDistribution(
                joinRequest.getPlayerId(),
                joinRequest.getPlayerName(),
                game
        );

        game.addPlayer(player);
        System.out.println("✅ Jugador añadido - ID: " + player.getId() +
                ", Posición: (" + player.getX() + "," + player.getY() + ")");

        game.getPlayers().forEach(p -> System.out.println("- " + p.getId()));

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

        System.out.println("[Controller] Ready recibido de: " + readyRequest.getPlayerId());

        Game game = gameRepository.findGameByCode(gameCode);
        if (game != null) {
            game.getPlayerById(readyRequest.getPlayerId())
                    .ifPresent(player -> {
                        player.setReady(readyRequest.isReady());
                        System.out.println("[Controller] Estado ready actualizado: " + player.getName() + " = " + player.isReady());
                    });
            messagingTemplate.convertAndSend("/topic/lobby/" + gameCode, createLobbyUpdate(game));
        }
    }

    @MessageMapping("/lobby/{gameCode}/start")
    public void handleStartGame(
            @Payload StartGameRequest startRequest,
            @DestinationVariable String gameCode) {

        System.out.println("[Controller] Solicitud de inicio recibida de: " + startRequest.getHostPlayerId());

        Game game = gameRepository.findGameByCode(gameCode);
        if (game == null) {
            System.out.println("[Controller] Juego no encontrado: " + gameCode);
            return;
        }

        if (!game.getHostPlayerId().equals(startRequest.getHostPlayerId())) {
            System.out.println("[Controller] El solicitante no es el host");
            return;
        }

        if (!game.canStart()) {
            System.out.println("[Controller] No se cumplen las condiciones para iniciar");
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

        System.out.println("[Controller] Partida iniciada: " + gameCode);
    }

    @MessageMapping("/game/{gameCode}/collect")
    public void handleCollectPowerUp(
            @Payload PowerUpCollectRequest collectRequest,
            @DestinationVariable String gameCode) {

        System.out.println("Recolección de power-up en: " +
                collectRequest.getX() + "," + collectRequest.getY());

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
