package ECI.ARSW.Model.Game.dto;

public class PlayerActionRequest {
    private String playerId;
    private PlayerActionDTO action;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public PlayerActionDTO getAction() {
        return action;
    }

    public void setAction(PlayerActionDTO action) {
        this.action = action;
    }
}