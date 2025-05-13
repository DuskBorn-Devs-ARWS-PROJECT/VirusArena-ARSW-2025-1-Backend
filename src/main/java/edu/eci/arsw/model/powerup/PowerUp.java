package edu.eci.arsw.model.powerup;

import edu.eci.arsw.model.*;
import edu.eci.arsw.model.player.Survivor;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PowerUp {
    private int x;
    private int y;
    protected final AtomicBoolean beingCollected;
    protected final long effectDuration;

    public PowerUp(int x, int y, long effectDuration) {
        this.x = x;
        this.y = y;
        this.beingCollected = new AtomicBoolean(false);
        this.effectDuration = effectDuration;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isBeingCollected() {
        return beingCollected.get();
    }

    public boolean acquire(Survivor survivor) {
        // Solo un hilo puede adquirir el power-up a la vez
        if (!beingCollected.compareAndSet(false, true)) {
            return false;
        }

        try {
            return tryAcquire(survivor);
        } finally {
            beingCollected.set(false);
        }
    }

    protected abstract boolean tryAcquire(Survivor survivor);
}