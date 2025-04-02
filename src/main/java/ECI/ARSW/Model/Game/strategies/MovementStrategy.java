package ECI.ARSW.Model.Game.strategies;

import ECI.ARSW.Model.Game.*;
import ECI.ARSW.Model.Player.*;

public interface MovementStrategy {
    boolean isValidMove(Player player, int newX, int newY, Game game);
    boolean isPathClear(Player player, int startX, int startY, int endX, int endY, Game game);
}