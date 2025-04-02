package ECI.ARSW.Model.Game.dto;

public class PlayerJoinRequest {
    private String gameCode;
    private String playerName;

    public String getGameCode() {
        return gameCode;
    }

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