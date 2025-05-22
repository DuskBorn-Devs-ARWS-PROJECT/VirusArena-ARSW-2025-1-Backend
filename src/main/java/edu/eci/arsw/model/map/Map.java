package edu.eci.arsw.model.map;

import edu.eci.arsw.model.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Map {
    private static final Logger logger = LoggerFactory.getLogger(Map.class);
    private int width;
    private int height;
    private char[][] grid;
    private Lock lock;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new char[height][width];
        this.lock = new ReentrantLock();
        initializeMap();
    }

    private void initializeMap() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i == 0 || i == height - 1 || j == 0 || j == width - 1) {
                    grid[i][j] = '#'; // Outer wall
                } else {
                    grid[i][j] = '.'; // Empty spaces
                }
            }
        }

        int[][] innerWalls = {
                {0,0}, {0,1}, {0,2}, {0,3}, {0,4}, {0,5}, {0,6}, {0,7}, {0,8}, {0,9}, {0,10}, {0,11}, {0,12}, {0,13}, {0,14}, {0,15}, {0,16}, {0,17}, {0,18}, {0,19}, {0,20}, {0,21}, {0,22}, {0,23}, {0,24}, {0,25}, {0,26}, {0,27}, {0,28}, {0,29}, {0,30}, {0,31}, {0,32},
                {1,0}, {1,5}, {1,32},
                {2,0}, {2,5}, {2,10}, {2,19}, {2,32},
                {3,0}, {3,5}, {3,32},
                {4,0}, {4,5}, {4,10}, {4,19}, {4,32},
                {5,0}, {5,5}, {5,10}, {5,19}, {5,23}, {5,24}, {5,25}, {5,26}, {5,27}, {5,29}, {5,30}, {5,31}, {5,32},
                {6,0}, {6,10}, {6,11}, {6,12}, {6,13}, {6,14}, {6,16}, {6,17}, {6,18}, {6,19}, {6,23}, {6,32},
                {7,0}, {7,10}, {7,13}, {7,19}, {7,23}, {7,32},
                {8,0}, {8,13}, {8,19}, {8,23}, {8,32},
                {9,0}, {9,1}, {9,2}, {9,3}, {9,4}, {9,5}, {9,6}, {9,7}, {9,8}, {9,9}, {9,10}, {9,13}, {9,19}, {9,23}, {9,32},
                {10,0}, {10,7}, {10,10}, {10,13}, {10,19}, {10,20}, {10,22}, {10,23}, {10,32},
                {11,0}, {11,10}, {11,13}, {11,19}, {11,32},
                {12,0}, {12,7}, {12,10}, {12,13}, {12,19}, {12,23}, {12,32},
                {13,0}, {13,7}, {13,10}, {13,13}, {13,14}, {13,15}, {13,16}, {13,17}, {13,18}, {13,19}, {13,23}, {13,32},
                {14,0}, {14,7}, {14,23}, {14,24}, {14,25}, {14,26}, {14,27}, {14,29}, {14,30}, {14,31}, {14,32},
                {15,0}, {15,7}, {15,10}, {15,32},
                {16,0}, {16,1}, {16,2}, {16,3}, {16,4}, {16,5}, {16,6}, {16,7}, {16,10}, {16,11}, {16,12}, {16,13}, {16,14}, {16,15}, {16,16}, {16,17}, {16,18}, {16,23}, {16,24}, {16,25}, {16,26}, {16,28}, {16,29}, {16,30}, {16,31}, {16,32},
                {17,0}, {17,10}, {17,18}, {17,23}, {17,32},
                {18,0}, {18,18}, {18,32},
                {19,0}, {19,10}, {19,18}, {19,23}, {19,32},
                {20,0}, {20,1}, {20,2}, {20,3}, {20,5}, {20,6}, {20,7}, {20,8}, {20,9}, {20,10}, {20,18}, {20,23}, {20,32},
                {21,0}, {21,3}, {21,10}, {21,18}, {21,23}, {21,32},
                {22,0}, {22,3}, {22,10}, {22,18}, {22,23}, {22,32},
                {23,0}, {23,3}, {23,10}, {23,11}, {23,12}, {23,13}, {23,14}, {23,15}, {23,17}, {23,18}, {23,23}, {23,24}, {23,25}, {23,26}, {23,27}, {23,28}, {23,29}, {23,31}, {23,32},
                {24,0}, {24,3}, {24,10}, {24,23}, {24,32},
                {25,0}, {25,3}, {25,10}, {25,32},
                {26,0}, {26,1}, {26,3}, {26,4}, {26,5}, {26,6}, {26,7}, {26,8}, {26,9}, {26,10}, {26,23}, {26,32},
                {27,0}, {27,8}, {27,14}, {27,15}, {27,16}, {27,17}, {27,18}, {27,19}, {27,21}, {27,22}, {27,23}, {27,32},
                {28,0}, {28,14}, {28,23}, {28,32},
                {29,0}, {29,8}, {29,14}, {29,23}, {29,32},
                {30,0}, {30,8}, {30,14}, {30,23}, {30,32},
                {31,0}, {31,8}, {31,14}, {31,23}, {31,32},
                {32,0}, {32,8}, {32,14}, {32,23}, {32,32}
        };

        for (int[] wall : innerWalls) {
            if (wall[0] < height && wall[1] < width) {
                grid[wall[0]][wall[1]] = '#';
            }
        }

        if (32 < height && 12 < width) {
            grid[32][12] = '.';
        }
    }

    public boolean isWalkable(int x, int y) {
        lock.lock();
        try {
            logger.debug("Verificando posición X:{} Y:{} - Valor: {}", x, y, grid[y][x]);            return x >= 0 && x < width && y >= 0 && y < height && grid[y][x] == '.';
        } finally {
            lock.unlock();
        }
    }

    public void setCell(int x, int y, char value) {
        lock.lock();
        try {
            logger.debug("Actualizando celda X:{} Y:{} a {}", x, y, value);            if (x >= 0 && x < width && y >= 0 && y < height) {
                grid[y][x] = value;
            }
        } finally {
            lock.unlock();
        }
    }

    public char getCell(int x, int y) {
        lock.lock();
        try {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                return grid[y][x];
            }
            return '#';
        } finally {
            lock.unlock();
        }
    }

    public void printMap() {
        lock.lock();
        try {
            if (logger.isDebugEnabled()) {
                StringBuilder mapString = new StringBuilder("\n");
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        mapString.append(grid[i][j]).append(" ");
                    }
                    mapString.append("\n");
                }
                logger.debug("Mapa actual:\n{}", mapString.toString());
            }
        } finally {
            lock.unlock();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public char[][] getGridCopy() {
        lock.lock();
        try {
            char[][] copy = new char[height][width];
            for (int i = 0; i < height; i++) {
                System.arraycopy(grid[i], 0, copy[i], 0, width);
            }
            return copy;
        } finally {
            lock.unlock();
        }
    }
    public void placePlayer(Player player) {
        lock.lock();
        try {
            if (isWalkable(player.getX(), player.getY())) {
                grid[player.getY()][player.getX()] = player.getSymbol();
            }
        } finally {
            lock.unlock();
        }
    }
}
