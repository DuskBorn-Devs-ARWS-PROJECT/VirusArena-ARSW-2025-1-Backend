package ECI.ARSW.Model.Game.strategies;

import ECI.ARSW.Model.Game.*;
import ECI.ARSW.Model.Player.*;

public class InfectedMovement implements MovementStrategy {
    @Override
    public boolean isValidMove(Player player, int newX, int newY, Game game) {
        int dx = Math.abs(newX - player.getX());
        int dy = Math.abs(newY - player.getY());

        if (dx > 2 || dy > 2) {
            return false;
        }

        if (dx > 1 || dy > 1) {
            return isPathClear(player, player.getX(), player.getY(), newX, newY, game);
        }

        return game.getMap().isWalkable(newX, newY) &&
                !game.isPositionOccupied(newX, newY, player);
    }

    @Override
    public boolean isPathClear(Player player, int startX, int startY, int endX, int endY, Game game) {
        if (startX == endX || startY == endY) {
            return isStraightPathClear(startX, startY, endX, endY, game);
        }
        return isDiagonalPathClear(startX, startY, endX, endY, game);
    }

    private boolean isStraightPathClear(int startX, int startY, int endX, int endY, Game game) {
        int stepX = Integer.compare(endX, startX);
        int stepY = Integer.compare(endY, startY);

        int x = startX + stepX;
        int y = startY + stepY;

        while (x != endX || y != endY) {
            if (!game.getMap().isWalkable(x, y)) {
                return false;
            }
            x += stepX;
            y += stepY;
        }
        return true;
    }

    private boolean isDiagonalPathClear(int startX, int startY, int endX, int endY, Game game) {
        int steps = Math.abs(endX - startX);
        int stepX = (endX > startX) ? 1 : -1;
        int stepY = (endY > startY) ? 1 : -1;

        for (int i = 1; i < steps; i++) {
            int checkX = startX + (i * stepX);
            int checkY = startY + (i * stepY);
            if (!game.getMap().isWalkable(checkX, checkY)) {
                return false;
            }
        }
        return true;
    }
}