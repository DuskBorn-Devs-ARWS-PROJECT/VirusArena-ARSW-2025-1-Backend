package ECI.ARSW.Model.Game.dto;

import ECI.ARSW.Model.Game.Game;

public class GameInfo {
    private final String gameCode;
    private final GameState state;

    public GameInfo(String gameCode, GameState state) {
        this.gameCode = gameCode;
        this.state = state;
    }

    public String getGameCode() {
        return gameCode;
    }

    public GameState getState() {
        return state;
    }
}