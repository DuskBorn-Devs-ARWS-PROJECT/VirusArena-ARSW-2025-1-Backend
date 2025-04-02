package ECI.ARSW.Model.Game;

public class PlayerAction {
    public enum ActionType { MOVE, USE_POWERUP, COLLECT }

    private String playerId;
    private ActionType type;
    private int x;
    private int y;

    public PlayerAction(String playerId, ActionType type, int x, int y) {
        this.playerId = playerId;
        this.type = type;
        this.x = x;
        this.y = y;
    }

    // Getters
    public String getPlayerId() { return playerId; }
    public ActionType getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }

    // Setters
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public void setType(ActionType type) { this.type = type; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}