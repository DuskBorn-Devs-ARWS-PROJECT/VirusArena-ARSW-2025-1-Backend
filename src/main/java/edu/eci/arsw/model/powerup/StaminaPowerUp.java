package edu.eci.arsw.model.powerup;


import edu.eci.arsw.model.player.Survivor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class StaminaPowerUp extends PowerUp {
    public static final long DEFAULT_DURATION = 10000;

    public StaminaPowerUp(int x, int y) {
        super(x, y, DEFAULT_DURATION);
    }
    private static final Logger logger = LoggerFactory.getLogger(StaminaPowerUp.class);
    @Override
    protected boolean tryAcquire(Survivor survivor) {
        if (survivor.hasStaminaActive()) {
            logger.warn("{} ya tiene un boost activo", survivor.getName());
            return false;
        }

        survivor.enableStamina(effectDuration);
        logger.info("{} obtuvo STAMINA BOOST por {} segundos",
                survivor.getName(), effectDuration/1000);
        return true;
    }
}