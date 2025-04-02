package ECI.ARSW.Model.Game;

import ECI.ARSW.Model.Map.Map;
import ECI.ARSW.Model.Game.dto.GameInfo;
import ECI.ARSW.Model.Game.dto.GameState;
import ECI.ARSW.Model.Game.dto.GameStateDTO;
import ECI.ARSW.Model.Game.dto.PlayerActionRequest;
import ECI.ARSW.Model.Game.dto.PlayerInfo;
import ECI.ARSW.Model.Game.dto.PlayerJoinRequest;
import ECI.ARSW.Model.Game.dto.PlayerReadyRequest;
import ECI.ARSW.Model.Game.dto.PlayerStateDTO;
import ECI.ARSW.Model.Game.dto.PowerUpDTO;
import ECI.ARSW.Model.Game.dto.GameStartRequest;
import ECI.ARSW.Model.Player.*;
import ECI.ARSW.Model.PowerUp.*;
import ECI.ARSW.Model.Game.services.GameNotificationService;
import ECI.ARSW.Model.Game.strategies.*;
import ECI.ARSW.Model.Game.dto.PlayerInfo;
import ECI.ARSW.Model.Game.dto.GameState;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;

public class Game {
    private final String gameCode;
    private final Map map;
    private final ConcurrentHashMap<String, Player> players;
    private final List<PowerUp> powerUps;
    private final Lock gameLock;
    private GameState state;
    private final ExecutorService playerThreadPool;
    private final ConcurrentHashMap<String, Boolean> readyPlayers;
    private final GameNotificationService notificationService;
    private Thread countdownThread;
    private final MovementStrategy survivorMovement;
    private final MovementStrategy infectedMovement;

    public Game(String gameCode, int width, int height, GameNotificationService notificationService) {
        this.gameCode = gameCode;
        this.map = new Map(width, height);
        this.players = new ConcurrentHashMap<>();
        this.powerUps = new CopyOnWriteArrayList<>();
        this.gameLock = new ReentrantLock();
        this.state = GameState.WAITING;
        this.playerThreadPool = Executors.newCachedThreadPool();
        this.readyPlayers = new ConcurrentHashMap<>();
        this.notificationService = notificationService;
        this.survivorMovement = new SurvivorMovement();
        this.infectedMovement = new InfectedMovement();
    }

    // Player management methods
    public void addPlayer(Player player) {
        gameLock.lock();
        try {
            if (state == GameState.WAITING && !players.containsKey(player.getId())) {
                players.put(player.getId(), player);
                assignStartingPosition(player);
                readyPlayers.put(player.getId(), false);
                playerThreadPool.execute(new PlayerHandler(player));
                notificationService.notifyGameUpdate(this);
            }
        } finally {
            gameLock.unlock();
        }
    }

    public void removePlayer(String playerId) {
        gameLock.lock();
        try {
            Player player = players.remove(playerId);
            if (player != null) {
                map.setCell(player.getX(), player.getY(), '.');
                readyPlayers.remove(playerId);
                notificationService.notifyGameUpdate(this);
                checkGameEnd();
            }
        } finally {
            gameLock.unlock();
        }
    }

    public Map getMap() {
        return this.map;
    }

    public void addPowerUp(PowerUp powerUp) {
        gameLock.lock();
        try {
            powerUps.add(powerUp);
            map.setCell(powerUp.getX(), powerUp.getY(), 'O');
        } finally {
            gameLock.unlock();
        }
    }

    public GameState getGameState() {
        return this.state;
    }

    private void assignStartingPosition(Player player) {
        Random rand = new Random();
        int x, y;
        do {
            x = rand.nextInt(map.getWidth());
            y = rand.nextInt(map.getHeight());
        } while (!map.isWalkable(x, y) || isPositionOccupied(x, y, null));

        player.setPosition(x, y);
        map.setCell(x, y, player.getSymbol());
    }

    // Ready/start management
    public void togglePlayerReady(String playerId) {
        gameLock.lock();
        try {
            readyPlayers.compute(playerId, (id, ready) -> ready == null ? true : !ready);
            notificationService.notifyGameUpdate(this);

            if (canStartGame() && state == GameState.WAITING) {
                startCountdown();
            }
        } finally {
            gameLock.unlock();
        }
    }

    public boolean canStartGame() {
        return players.size() >= 2 &&
                readyPlayers.size() == players.size() &&
                readyPlayers.values().stream().allMatch(Boolean::booleanValue);
    }

    public void startCountdown() {
        gameLock.lock();
        try {
            if (state == GameState.WAITING && canStartGame()) {
                state = GameState.COUNTDOWN;
                notificationService.notifyGameUpdate(this);

                countdownThread = new Thread(() -> {
                    try {
                        for (int i = 5; i > 0; i--) {
                            notificationService.sendCountdownUpdate(gameCode, i);
                            Thread.sleep(1000);
                        }
                        startGame();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                countdownThread.start();
            }
        } finally {
            gameLock.unlock();
        }
    }

    private void startGame() {
        gameLock.lock();
        try {
            if (state == GameState.COUNTDOWN) {
                state = GameState.IN_PROGRESS;
                spawnInitialPowerUps();
                notificationService.notifyGameUpdate(this);
            }
        } finally {
            gameLock.unlock();
        }
    }

    // Game logic
    public void processPlayerAction(String playerId, PlayerAction action) {
        gameLock.lock();
        try {
            Player player = players.get(playerId);
            if (player != null && state == GameState.IN_PROGRESS) {
                switch (action.getType()) {
                    case MOVE:
                        handleMove(player, action.getX(), action.getY());
                        break;
                    case USE_POWERUP:
                        handlePowerUp(player);
                        break;
                    case COLLECT:
                        handlePowerUpCollection(player);
                        break;
                }
                notificationService.notifyGameUpdate(this);
                checkGameEnd();
            }
        } finally {
            gameLock.unlock();
        }
    }

    private void handleMove(Player player, int newX, int newY) {
        MovementStrategy strategy = (player instanceof Survivor) ? survivorMovement : infectedMovement;

        if (!strategy.isValidMove(player, newX, newY, this)) {
            return;
        }

        map.setCell(player.getX(), player.getY(), '.');
        player.setPosition(newX, newY);
        map.setCell(newX, newY, player.getSymbol());

        checkGameEvents(player);
    }

    private void checkGameEvents(Player player) {
        if (player instanceof Infected) {
            checkInfections((Infected) player);
        }
    }

    private void checkInfections(Infected infected) {
        players.values().stream()
                .filter(p -> p instanceof Survivor)
                .map(p -> (Survivor) p)
                .filter(s -> Math.abs(s.getX() - infected.getX()) <= 1 &&
                        Math.abs(s.getY() - infected.getY()) <= 1)
                .forEach(s -> infectPlayer(infected, s));
    }

    private void infectPlayer(Infected infected, Survivor survivor) {
        removePlayer(survivor.getId());
        addPlayer(new Infected(survivor.getX(), survivor.getY(), survivor.getName()));
    }

    private void handlePowerUpCollection(Player player) {
        if (player instanceof Survivor) {
            Survivor survivor = (Survivor) player;

            Iterator<PowerUp> iterator = powerUps.iterator();
            while (iterator.hasNext()) {
                PowerUp powerUp = iterator.next();
                if (powerUp.getX() == survivor.getX() &&
                        powerUp.getY() == survivor.getY()) {

                    if (powerUp.acquire(survivor)) {
                        iterator.remove();
                        map.setCell(powerUp.getX(), powerUp.getY(), '.');
                        survivor.collectPowerUp();
                    }
                    break;
                }
            }
        }
    }

    private void handlePowerUp(Player player) {
        if (player instanceof Survivor) {
            Survivor survivor = (Survivor) player;
            if (survivor.usePowerUp()) {
                notificationService.sendPowerUpActivation(gameCode, player.getId());
            }
        }
    }

    public void spawnInitialPowerUps() {
        int powerUpCount = players.size() * 2;
        for (int i = 0; i < powerUpCount; i++) {
            spawnRandomPowerUp(new StaminaPowerUp(0, 0));
        }
    }

    public void spawnRandomPowerUp(PowerUp powerUp) {
        Random rand = new Random();
        int x, y;
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;

        do {
            x = rand.nextInt(map.getWidth());
            y = rand.nextInt(map.getHeight());
            attempts++;
        } while ((!map.isWalkable(x, y) || isPositionOccupied(x, y, null)) && attempts < MAX_ATTEMPTS);

        if (attempts < MAX_ATTEMPTS) {
            powerUp.setPosition(x, y);
            powerUps.add(powerUp);
            map.setCell(x, y, 'O');
        }
    }

    public boolean isPositionOccupied(int x, int y, Player excludingPlayer) {
        gameLock.lock();
        try {
            boolean playerOccupied = players.values().stream()
                    .anyMatch(p -> (excludingPlayer == null || p != excludingPlayer) &&
                            p.getX() == x && p.getY() == y);

            boolean powerUpOccupied = powerUps.stream()
                    .anyMatch(p -> p.getX() == x && p.getY() == y);

            return playerOccupied || powerUpOccupied;
        } finally {
            gameLock.unlock();
        }
    }

    public boolean isPathClear(int startX, int startY, int endX, int endY, Player player) {
        MovementStrategy strategy = (player instanceof Survivor) ? survivorMovement : infectedMovement;
        return strategy.isPathClear(player, startX, startY, endX, endY, this);
    }

    private void checkGameEnd() {
        long survivors = players.values().stream()
                .filter(p -> p instanceof Survivor)
                .count();

        if (survivors == 0) {
            endGame("Todos los jugadores han sido infectados!");
        } else if (survivors == 1 && players.size() > 1) {
            Player winner = players.values().stream()
                    .filter(p -> p instanceof Survivor)
                    .findFirst()
                    .orElse(null);
            if (winner != null) {
                endGame("El jugador " + winner.getName() + " es el último superviviente!");
            }
        }
    }

    private void endGame(String message) {
        gameLock.lock();
        try {
            state = GameState.FINISHED;
            notificationService.sendGameEnd(gameCode, message, getWinnerInfo());
            playerThreadPool.shutdown();
            if (countdownThread != null && countdownThread.isAlive()) {
                countdownThread.interrupt();
            }
        } finally {
            gameLock.unlock();
        }
    }

    private List<PlayerInfo> getWinnerInfo() {
        return players.values().stream()
                .map(p -> new PlayerInfo(p.getId(), p.getName(), true, p instanceof Infected))
                .collect(Collectors.toList());
    }

    public void removePowerUp(PowerUp powerUp) {
        gameLock.lock();
        try {
            if (powerUps.remove(powerUp)) {
                map.setCell(powerUp.getX(), powerUp.getY(), '.');
            }
        } finally {
            gameLock.unlock();
        }
    }

    public char[][] getGridCopy() {
        return map.getGridCopy();
    }

    public void printMap() {
        map.printMap();
    }

    public String getState() {
        return state.toString();
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public List<PowerUp> getPowerUps() {
        return new ArrayList<>(powerUps);
    }

    public char[][] getMapRepresentation() {
        return map.getGridCopy();
    }

    public String getGameCode() {
        return gameCode;
    }

    private class PlayerHandler implements Runnable {
        private final Player player;
        private final Lock actionLock = new ReentrantLock();

        public PlayerHandler(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            try {
                while (state != GameState.FINISHED) {
                    actionLock.lock();
                    try {
                        Thread.sleep(100);
                    } finally {
                        actionLock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}