package edu.eci.arsw.model.player;

import edu.eci.arsw.model.Game;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter @Setter
public class Survivor extends Player {
    private final Lock collectionLock = new ReentrantLock();
    private boolean staminaActive;
    private long staminaEndTime;
    private int powerUpCount;
    private boolean hasPowerUp;
    private static final int ORIGINAL_SPEED = 1;
    private static final int BOOSTED_SPEED = 2;

    public Survivor(String id, int x, int y, String name) {
        super(id, x, y, 1, name);
        this.staminaActive = false;
        this.powerUpCount = 0;
        this.hasPowerUp = false;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Survivor survivor = (Survivor) o;
        return staminaActive == survivor.staminaActive &&
                staminaEndTime == survivor.staminaEndTime &&
                powerUpCount == survivor.powerUpCount &&
                hasPowerUp == survivor.hasPowerUp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), staminaActive, staminaEndTime, powerUpCount, hasPowerUp);
    }

    @Override
    public char getSymbol() {
        return 'S';
    }

    @Override
    public boolean canMoveTo(int newX, int newY, Game game) {
        int dx = Math.abs(newX - getX());
        int dy = Math.abs(newY - getY());

        int currentSpeed = staminaActive ? 2 : 1;

        if (dx > currentSpeed || dy > currentSpeed) {
            return false;
        }

        return isValidMove(newX, newY, game);
    }

    @Override
    public void collectPowerUp() {
        collectionLock.lock();
        try {
            this.powerUpCount++;
            this.hasPowerUp = true;
        } finally {
            collectionLock.unlock();
        }
    }

    public boolean usePowerUp() {
        collectionLock.lock();
        try {
            if (powerUpCount > 0 && !staminaActive) {
                powerUpCount--;
                enableStamina(10000);
            }
            return false;
        } finally {
            collectionLock.unlock();
        }
    }

    public void enableStamina(long durationMillis) {
        collectionLock.lock();
        try {
            this.staminaActive = true;
            this.staminaEndTime = System.currentTimeMillis() + durationMillis;
            setSpeed(BOOSTED_SPEED);
            new Thread(this::checkStaminaDuration).start();
        } finally {
            collectionLock.unlock();
        }
    }

    private void checkStaminaDuration() {
        while (System.currentTimeMillis() < staminaEndTime) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        disableStamina();
    }

    private void disableStamina() {
        collectionLock.lock();
        try {
            this.staminaActive = false;
            setSpeed(ORIGINAL_SPEED);
        } finally {
            collectionLock.unlock();
        }
    }

    public int getPowerUpCount() {
        collectionLock.lock();
        try {
            return powerUpCount;
        } finally {
            collectionLock.unlock();
        }
    }

    public boolean hasStaminaActive() {
        return staminaActive;
    }

    public boolean hasPowerUp() {
        return hasPowerUp;
    }
}