package ECI.ARSW.Model.Player;

import ECI.ARSW.Model.Game.*;
import java.util.UUID;

public abstract class Player {
    private final String id;
    private int x;
    private int y;
    private int speed;
    private final String name;
    private boolean ready;

    public Player(int x, int y, int speed, String name) {
        this.id = UUID.randomUUID().toString();
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.name = name;
        this.ready = false;
    }

    // Getters y setters
    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public String getName() { return name; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public abstract char getSymbol();
    public abstract boolean canMoveTo(int newX, int newY, Game game);

    protected boolean isValidMove(int newX, int newY, Game game) {
        return game.getMap().isWalkable(newX, newY) &&
                game.getPlayers().stream()
                        .noneMatch(p -> p != this && p.getX() == newX && p.getY() == newY);
    }

    public void collectPowerUp() {
        // Implementación según tu lógica de juego
        if (this instanceof Survivor) {
            ((Survivor) this).enableStamina(10000); // 10 segundos
        }
    }
    public boolean isReady() {
        return this.ready;
    }
}