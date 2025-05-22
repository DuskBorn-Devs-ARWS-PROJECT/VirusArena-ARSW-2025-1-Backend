package edu.eci.arsw.model.dto;

import edu.eci.arsw.model.player.*;

import java.util.ArrayList;
import java.util.List;

public class GameDTOs {

    public static class PlayerJoinRequest {
        private String playerId;
        private String gameCode;
        private String playerName;

        public String getGameCode() {
            return gameCode;
        }

        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }

        public void setGameCode(String gameCode) {
            this.gameCode = gameCode;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }
    }

    public static class PlayerActionRequest {
        private String playerId;
        private PlayerAction action;

        public String getPlayerId() {
            return playerId;
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }

        public PlayerAction getAction() {
            return action;
        }

        public void setAction(PlayerAction action) {
            this.action = action;
        }
    }

    public static class GameInfo {
        private final String gameCode;
        private final String state;

        public GameInfo(String gameCode, String state) {
            this.gameCode = gameCode;
            this.state = state;
        }

        public String getGameCode() {
            return gameCode;
        }

        public String getState() {
            return state;
        }
    }

    public static class GameStateDTO {
        private String gameCode;
        private String state;
        private char[][] board;
        private List<PlayerInfoDTO> players;
        private long remainingTimeMillis;

        public GameStateDTO(String gameCode, String state, char[][] board, List<Player> players, long remainingTimeMillis) {
            this.gameCode = gameCode;
            this.state = state;
            this.board = board;
            this.players = new ArrayList<>();
            for (Player player : players) {
                this.players.add(new PlayerInfoDTO(player));
            }
            this.remainingTimeMillis = remainingTimeMillis;
        }

        public String getGameCode() { return gameCode; }
        public String getState() { return state; }
        public char[][] getBoard() { return board; }
        public List<PlayerInfoDTO> getPlayers() { return players; }
        public long getRemainingTimeMillis() { return remainingTimeMillis; }
    }

    public static class PowerUpCollectRequest {
        private String playerId;
        private int x;
        private int y;

        // Getters y setters
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
    }

    public static class PlayerInfoDTO {
        private String id;
        private String name;
        private int x;
        private int y;
        private String type; // "INFECTED" o "SURVIVOR"
        private int powerUpCount;
        private boolean staminaActive;

        public PlayerInfoDTO(Player player) {
            this.id = player.getId();
            this.name = player.getName();
            this.x = player.getX();
            this.y = player.getY();
            this.type = player instanceof Infected ? "INFECTED" : "SURVIVOR";

            if (player instanceof Survivor survivor) {
                this.powerUpCount = survivor.getPowerUpCount();
                this.staminaActive = survivor.hasStaminaActive();
            } else {
                this.powerUpCount = 0;
                this.staminaActive = false;
            }
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public int getX() { return x; }
        public int getY() { return y; }
        public String getType() { return type; }
        public int getPowerUpCount() { return powerUpCount; }
        public boolean isStaminaActive() { return staminaActive; }
    }


    public static class LobbyUpdate {
        private List<PlayerInfoDTO> players;
        private String hostPlayerId;
        private String gameState;

        public LobbyUpdate(List<Player> players, String hostPlayerId, String gameState) {
            this.players = new ArrayList<>();
            for (Player player : players) {
                this.players.add(new PlayerInfoDTO(player));
            }
            this.hostPlayerId = hostPlayerId;
            this.gameState = gameState;
        }

        // Getters
        public List<PlayerInfoDTO> getPlayers() { return players; }
        public String getHostPlayerId() { return hostPlayerId; }
        public String getGameState() { return gameState; }
    }


    public static class PlayerReadyRequest {
        private String playerId;
        private boolean isReady;

        public String getPlayerId() {
            return playerId;
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }

        public boolean isReady() {
            return isReady;
        }

        public void setReady(boolean ready) {
            isReady = ready;
        }
    }

    public static class StartGameRequest {
        private String hostPlayerId;

        public String getHostPlayerId() {
            return hostPlayerId;
        }

        public void setHostPlayerId(String hostPlayerId) {
            this.hostPlayerId = hostPlayerId;
        }
    }

    public enum PlayerAction {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        USE_POWERUP
    }
}
