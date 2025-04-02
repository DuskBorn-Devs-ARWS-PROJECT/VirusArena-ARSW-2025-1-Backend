package ECI.ARSW.Controller;

import ECI.ARSW.Model.Game.*;
import ECI.ARSW.Model.Game.Game;
import ECI.ARSW.Model.Game.dto.*;
import ECI.ARSW.Model.Game.dto.PlayerInfo;
import ECI.ARSW.Model.Game.services.*;
import ECI.ARSW.Model.Game.exceptions.GameException;
import ECI.ARSW.Model.Player.*;
import ECI.ARSW.Model.PowerUp.*;
import ECI.ARSW.Model.Game.dto.GameStateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/game")
public class GameManager {
    private final GameRepository gameRepository;
    private final GameNotificationService notificationService;
    private final PlayerFactory playerFactory;

    @Autowired
    public GameManager(GameRepository gameRepository,
                       GameNotificationService notificationService,
                       PlayerFactory playerFactory) {
        this.gameRepository = gameRepository;
        this.notificationService = notificationService;
        this.playerFactory = playerFactory;
    }

    @MessageMapping("/game/join")
    @SendToUser("/queue/join")
    public GameInfo handleJoinGame(@Payload PlayerJoinRequest joinRequest, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("Solicitud de unión recibida: " + joinRequest.getPlayerName());

            validateJoinRequest(joinRequest);

            boolean isNewGame = joinRequest.getGameCode() == null || joinRequest.getGameCode().trim().isEmpty() || joinRequest.getGameCode().equals("NEW_GAME");
            Game game;
            if (isNewGame) {
                String gameCode = generateGameCode();
                game = new Game(gameCode, 33, 33, notificationService);
                gameRepository.saveGame(game);
                System.out.println("Nueva partida creada: " + gameCode);
            } else {
                if (!gameRepository.containsGame(joinRequest.getGameCode())) {
                    System.out.println("Partida no encontrada: " + joinRequest.getGameCode());
                    throw new GameException("Game not found");
                }
                game = gameRepository.findGameByCode(joinRequest.getGameCode());
                System.out.println("Unido a partida existente: " + joinRequest.getGameCode());
            }

            Player player = createPlayer(joinRequest);
            game.addPlayer(player);
            gameRepository.saveGame(game);
            System.out.println("Jugador añadido: " + player.getName() + " a la partida " + game.getGameCode());

            boolean isHost = game.getPlayers().size() == 1;
            sendJoinConfirmation(headerAccessor, game, player, isHost);

            notificationService.notifyGameUpdate(game);
            notificationService.notifyPlayersUpdate(game);

            return new GameInfo(game.getGameCode(), game.getGameState());

        } catch (GameException e) {
            System.out.println("Error al unir jugador: " + e.getMessage());
            notificationService.sendError(headerAccessor.getSessionId(), e.getMessage());
            return null;
        }
    }


    @MessageMapping("/game/{gameCode}/action")
    public void handlePlayerAction(
            @DestinationVariable String gameCode,
            @RequestBody PlayerActionRequest actionRequest) {
        try {
            Game game = gameRepository.findGameByCode(gameCode);
            validateActionRequest(actionRequest);

            PlayerAction action = createPlayerAction(actionRequest);
            game.processPlayerAction(actionRequest.getPlayerId(), action);

            gameRepository.saveGame(game);
            notificationService.notifyGameUpdate(game);
        } catch (GameException e) {
            notificationService.sendErrorToPlayer(gameCode, actionRequest.getPlayerId(), e.getMessage());
        }
    }

    @MessageMapping("/game/{gameCode}/ready")
    public void handlePlayerReady(
            @DestinationVariable String gameCode,
            @RequestBody PlayerReadyRequest request) {
        try {
            Game game = gameRepository.findGameByCode(gameCode);
            validateReadyRequest(request);

            game.togglePlayerReady(request.getPlayerId());
            gameRepository.saveGame(game);

            notificationService.notifyGameUpdate(game);
            notificationService.notifyPlayersUpdate(game);
        } catch (GameException e) {
            notificationService.sendErrorToPlayer(gameCode, request.getPlayerId(), e.getMessage());
        }
    }

    @MessageMapping("/game/{gameCode}/start")
    public void handleGameStart(
            @DestinationVariable String gameCode,
            @RequestBody GameStartRequest request) {
        try {
            Game game = gameRepository.findGameByCode(gameCode);
            validateStartRequest(game);

            game.startCountdown();
            gameRepository.saveGame(game);
        } catch (GameException e) {
            notificationService.sendErrorToPlayer(gameCode, null, e.getMessage());
        }
    }

    @GetMapping("/create")
    public ResponseEntity<?> createGame() {
        String gameCode = generateGameCode();
        Game game = new Game(gameCode, 33, 33, notificationService);
        gameRepository.saveGame(game);
        return ResponseEntity.ok(new GameInfo(gameCode, game.getGameState()));
    }

    @GetMapping("/{gameCode}/state")
    public ResponseEntity<?> getGameState(@PathVariable String gameCode) {
        try {
            Game game = gameRepository.findGameByCode(gameCode);
            return ResponseEntity.ok(createGameStateDTO(game));
        } catch (GameException e) {
            return errorResponse(e.getMessage());
        }
    }

    @GetMapping("/{gameCode}/players")
    public ResponseEntity<?> getPlayers(@PathVariable String gameCode) {
        try {
            Game game = gameRepository.findGameByCode(gameCode);
            List<PlayerInfo> players = game.getPlayers().stream()
                    .map(this::convertToPlayerInfo)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(players);
        } catch (GameException e) {
            return errorResponse(e.getMessage());
        }
    }

    @PostMapping("/{gameCode}/start")
    public ResponseEntity<?> manuallyStartGame(@PathVariable String gameCode) {
        try {
            Game game = gameRepository.findGameByCode(gameCode);
            validateStartRequest(game);

            game.startCountdown();
            gameRepository.saveGame(game);
            return ResponseEntity.ok().build();
        } catch (GameException e) {
            return errorResponse(e.getMessage());
        }
    }

    // Métodos auxiliares privados
    private void validateJoinRequest(PlayerJoinRequest request) throws GameException {
        if (request.getPlayerName() == null || request.getPlayerName().trim().isEmpty()) {
            throw new GameException("Player name is required");
        }
    }

    private Game getOrCreateGame(PlayerJoinRequest request) throws GameException {
        if (request.getGameCode() == null || request.getGameCode().trim().isEmpty() || request.getGameCode().equals("NEW_GAME")) {
            String gameCode = generateGameCode();
            Game game = new Game(gameCode, 33, 33, notificationService);
            gameRepository.saveGame(game);  // Guarda el juego en el repositorio
            return game;
        } else {
            if (!gameRepository.containsGame(request.getGameCode())) {
                throw new GameException("Game not found");
            }
            return gameRepository.findGameByCode(request.getGameCode());
        }
    }

    private Player createPlayer(PlayerJoinRequest request) {
        String type = (request.getGameCode() == null || request.getGameCode().isEmpty()) ?
                "infected" : "survivor";
        return playerFactory.createPlayer(type, 0, 0, request.getPlayerName());
    }

    private void sendJoinConfirmation(SimpMessageHeaderAccessor headerAccessor,
                                      Game game, Player player, boolean isHost) {
        PlayerStateDTO playerState = new PlayerStateDTO(
                player.getId(),
                player.getName(),
                player.getX(),
                player.getY(),
                player instanceof Infected,
                player.isReady(),
                player instanceof Survivor ? ((Survivor) player).getPowerUpCount() : 0
        );

        notificationService.sendJoinConfirmation(
                headerAccessor.getSessionId(),
                game.getGameCode(),
                playerState,
                isHost
        );
    }

    private void validateActionRequest(PlayerActionRequest request) throws GameException {
        if (request.getPlayerId() == null || request.getPlayerId().isEmpty()) {
            throw new GameException("Player ID is required");
        }
        if (request.getAction() == null) {
            throw new GameException("Action is required");
        }
    }

    private PlayerAction createPlayerAction(PlayerActionRequest request) {
        PlayerAction.ActionType actionType = PlayerAction.ActionType.valueOf(request.getAction().getType().toUpperCase());
        return new PlayerAction(
                request.getPlayerId(),
                actionType,
                request.getAction().getX(),
                request.getAction().getY()
        );
    }

    private void validateReadyRequest(PlayerReadyRequest request) throws GameException {
        if (request.getPlayerId() == null || request.getPlayerId().isEmpty()) {
            throw new GameException("Player ID is required");
        }
    }

    private void validateStartRequest(Game game) throws GameException {
        if (game.getGameState() != GameState.WAITING) {
            throw new GameException("Game is not in waiting state");
        }
        if (!game.canStartGame()) {
            throw new GameException("Not all players are ready or not enough players");
        }
    }

    private GameStateDTO createGameStateDTO(Game game) {
        GameStateDTO dto = new GameStateDTO();
        dto.setGameCode(game.getGameCode());
        dto.setState(game.getGameState());
        dto.setMap(game.getMapRepresentation());

        dto.setPlayers(game.getPlayers().stream()
                .map(this::convertToPlayerStateDTO)
                .collect(Collectors.toList()));

        dto.setPowerUps(convertPowerUpsToDTO(game.getPowerUps()));
        return dto;
    }

    private PlayerStateDTO convertToPlayerStateDTO(Player player) {
        return new PlayerStateDTO(
                player.getId(),
                player.getName(),
                player.getX(),
                player.getY(),
                player instanceof Infected,
                player.isReady(),
                player instanceof Survivor ? ((Survivor) player).getPowerUpCount() : 0
        );
    }

    private PlayerInfo convertToPlayerInfo(Player player) {
        return new PlayerInfo(
                player.getId(),
                player.getName(),
                player.isReady(),
                player instanceof Infected
        );
    }

    private List<PowerUpDTO> convertPowerUpsToDTO(List<PowerUp> powerUps) {
        return powerUps.stream()
                .map(p -> new PowerUpDTO(p.getX(), p.getY(), p.getClass().getSimpleName()))
                .collect(Collectors.toList());
    }

    private ResponseEntity<?> errorResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("error", message));
    }

    private String generateGameCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) {
            int index = (int) (rnd.nextFloat() * characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }
}