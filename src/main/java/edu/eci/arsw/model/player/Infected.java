package edu.eci.arsw.model.player;

import edu.eci.arsw.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Infected extends Player {
    // Constructor actualizado
    public Infected(String id, int x, int y, String name) {
        super(id, x, y, 1, name);
    }
    private static final Logger logger = LoggerFactory.getLogger(Infected.class);
    @Override
    public char getSymbol() {
        return 'I';
    }

    @Override
    public boolean canMoveTo(int newX, int newY, Game game) {
        int dx = Math.abs(newX - getX());
        int dy = Math.abs(newY - getY());

        if (dx > getSpeed() || dy > getSpeed()) {
            return false;
        }

        if (!isValidMove(newX, newY, game)) {
            return false;
        }

        if (dx > 1 || dy > 1) {
            return isPathClear(getX(), getY(), newX, newY, game);
        }

        return true;
    }

    private boolean isPathClear(int startX, int startY, int endX, int endY, Game game) {
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

    public void infect(Survivor survivor, Game game) {
        int dx = Math.abs(getX() - survivor.getX());
        int dy = Math.abs(getY() - survivor.getY());

        if (dx <= 1 && dy <= 1) {
            logger.info("{} ha infectado a {}", getName(), survivor.getName());
            game.removePlayer(survivor.getId());
            game.addPlayer(new Infected(
                    survivor.getId(),
                    survivor.getX(),
                    survivor.getY(),
                    survivor.getName()
            ));
        }
    }
}