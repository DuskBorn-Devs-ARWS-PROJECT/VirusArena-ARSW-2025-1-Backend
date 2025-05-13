package edu.eci.arsw.service;

import edu.eci.arsw.model.Game;
import edu.eci.arsw.model.dto.GameDTOs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class GameNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GameNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyGameUpdate(Game game) {
        GameDTOs.GameStateDTO gameState = convertToDTO(game);
        messagingTemplate.convertAndSend("/topic/game/" + game.getGameCode() + "/update", gameState);
    }

    private GameDTOs.GameStateDTO convertToDTO(Game game) {
        return new GameDTOs.GameStateDTO(
                game.getGameCode(),
                game.getState().toString(),
                game.getMap().getGridCopy(),
                new ArrayList<>(game.getPlayers()),
                game.getRemainingTimeMillis()
        );
    }
}
