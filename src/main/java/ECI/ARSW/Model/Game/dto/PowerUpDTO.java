package ECI.ARSW.Model.Game.dto;

public class PowerUpDTO {
    private int x;
    private int y;
    private String type;

    public PowerUpDTO(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public String getType() { return type; }
}