package ECI.ARSW.Model.PowerUp;

import ECI.ARSW.Model.Player.*;

public class StaminaPowerUp extends PowerUp {
    public static final long DEFAULT_DURATION = 10000; // 10 segundos

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