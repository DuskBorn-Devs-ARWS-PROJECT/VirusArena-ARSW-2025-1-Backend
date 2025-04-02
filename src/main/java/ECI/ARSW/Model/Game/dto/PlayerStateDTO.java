package ECI.ARSW.Model.Game.dto;

public class PlayerStateDTO {
    private final String id;
    private final String name;
    private final int x;
    private final int y;
    private final boolean infected;
    private final boolean ready;
    private final int powerUpCount;

    public PlayerStateDTO(String id, String name, int x, int y,
                          boolean infected, boolean ready, int powerUpCount) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.infected = infected;
        this.ready = ready;
        this.powerUpCount = powerUpCount;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isInfected() { return infected; }
    public boolean isReady() { return ready; }
    public int getPowerUpCount() { return powerUpCount; }
}