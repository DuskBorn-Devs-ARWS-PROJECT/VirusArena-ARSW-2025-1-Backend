package ECI.ARSW.Model.Game.services;

import ECI.ARSW.Model.Game.Game;
import ECI.ARSW.Model.Player.Survivor;
import ECI.ARSW.Model.PowerUp.PowerUp;
import ECI.ARSW.Model.Game.Game;
import ECI.ARSW.Model.Game.dto.GameStateDTO;
import ECI.ARSW.Model.Game.dto.PowerUpDTO;
import ECI.ARSW.Model.Game.dto.PlayerInfo;
import ECI.ARSW.Model.Game.dto.PlayerStateDTO;
import ECI.ARSW.Model.Player.Infected;
import ECI.ARSW.Model.Player.Player;
import ECI.ARSW.Model.Player.Survivor;
import ECI.ARSW.Model.PowerUp.PowerUp;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
public class GameNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public GameNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyGameUpdate(Game game) {
        GameStateDTO gameDTO = convertToDTO(game);
        messagingTemplate.convertAndSend("/topic/game/" + game.getGameCode() + "/update", gameDTO);
    }

    public void notifyPlayersUpdate(Game game) {
        messagingTemplate.convertAndSend("/topic/game/" + game.getGameCode() + "/players",
                game.getPlayers().stream()
                        .map(p -> new PlayerInfo(
                                p.getId(),
                                p.getName(),
                                p instanceof Infected,
                                p.isReady()))
                        .collect(Collectors.toList()));
    }

    private GameStateDTO convertToDTO(Game game) {
        GameStateDTO dto = new GameStateDTO();
        dto.setGameCode(game.getGameCode());
        dto.setState(game.getGameState());
        dto.setMap(game.getMapRepresentation());

        dto.setPlayers(game.getPlayers().stream()
                .map(p -> {
                    boolean isInfected = p instanceof Infected;
                    int powerUpCount = isInfected ? 0 : ((Survivor) p).getPowerUpCount();
                    return new PlayerStateDTO(
                            p.getId(),
                            p.getName(),
                            p.getX(),
                            p.getY(),
                            isInfected,
                            p.isReady(),
                            powerUpCount);
                })
                .collect(Collectors.toList()));

        dto.setPowerUps(game.getPowerUps().stream()
                .map(p -> new PowerUpDTO(p.getX(), p.getY(), p.getClass().getSimpleName()))
                .collect(Collectors.toList()));

        return dto;
    }

    private List<PowerUpDTO> convertPowerUpsToDTO(List<PowerUp> powerUps) {
        // Implementación para convertir power-ups a DTOs
        return powerUps.stream()
                .map(p -> new PowerUpDTO(p.getX(), p.getY(), p.getClass().getSimpleName()))
                .collect(Collectors.toList());
    }

    public void sendCountdownUpdate(String gameCode, int count) {
        messagingTemplate.convertAndSend("/topic/game/"+gameCode+"/countdown", count);
    }

    public void sendPowerUpActivation(String gameCode, String playerId) {
        messagingTemplate.convertAndSend("/topic/game/"+gameCode+"/powerup", playerId);
    }

    public void sendGameEnd(String gameCode, String message, List<PlayerInfo> winners) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("winners", winners);
        messagingTemplate.convertAndSend("/topic/game/"+gameCode+"/end", payload);
    }

    public void sendError(String sessionId, String message) {
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors",
                Collections.singletonMap("error", message));
    }

    public void sendErrorToPlayer(String gameCode, String playerId, String message) {
        messagingTemplate.convertAndSend("/topic/errors/" + gameCode,
                Collections.singletonMap("error", message));
    }

    public void sendJoinConfirmation(String sessionId, String gameCode,
                                     PlayerStateDTO playerState, boolean isHost) {
        Map<String, Object> response = new HashMap<>();
        response.put("gameCode", gameCode);
        response.put("playerId", playerState.getId());
        response.put("isHost", isHost);
        response.put("isInfected", playerState.isInfected());
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/join", response);
    }
}