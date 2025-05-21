package edu.eci.arsw.model.powerup;

import edu.eci.arsw.model.*;
import edu.eci.arsw.model.player.Survivor;

public class StaminaPowerUp extends PowerUp {
    public static final long DEFAULT_DURATION = 10000;

    public StaminaPowerUp(int x, int y) {
        super(x, y, DEFAULT_DURATION);
    }

    @Override
    protected boolean tryAcquire(Survivor survivor) {
        if (survivor.hasStaminaActive()) {
            System.out.println(survivor.getName() + " ya tiene un boost activo");
            return false;
        }

        survivor.enableStamina(effectDuration);
        System.out.println(survivor.getName() + " obtuvo STAMINA BOOST por " +
                (effectDuration/1000) + " segundos!");
        return true;
    }
}