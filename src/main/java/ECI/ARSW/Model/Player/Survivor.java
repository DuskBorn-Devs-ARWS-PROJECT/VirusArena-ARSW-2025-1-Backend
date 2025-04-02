package ECI.ARSW.Model.Player;

import ECI.ARSW.Model.Game.*;
import lombok.Getter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class Survivor extends Player {
    private final Lock collectionLock = new ReentrantLock();
    private boolean staminaActive;
    private long staminaEndTime;
    private int powerUpCount;
    private boolean hasPowerUp;

    public Survivor(int x, int y, String name) {
        super(x, y, 1, name);
        this.staminaActive = false;
        this.powerUpCount = 0;
        this.hasPowerUp = false;
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

    public void collectPowerUp() {
        collectionLock.lock();
        try {
            this.powerUpCount++;
            this.hasPowerUp = true;
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

    public boolean usePowerUp() {
        collectionLock.lock();
        try {
            if (powerUpCount > 0) {
                powerUpCount--;
                if (powerUpCount == 0) {
                    hasPowerUp = false;
                }
                return true;
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
            setSpeed(2);
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
            setSpeed(1);
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