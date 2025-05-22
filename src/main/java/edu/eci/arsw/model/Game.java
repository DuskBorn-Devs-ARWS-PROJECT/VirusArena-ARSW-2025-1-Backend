package edu.eci.arsw.model;

import edu.eci.arsw.model.map.Map;
import edu.eci.arsw.model.dto.GameDTOs;
import edu.eci.arsw.model.player.Player;
import edu.eci.arsw.model.player.Survivor;
import edu.eci.arsw.model.player.Infected;
import edu.eci.arsw.service.GameNotificationService;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game {
    private static final Logger logger = LoggerFactory.getLogger(Game.class);

    private final String gameCode;
    private final Map map;
    private final ConcurrentHashMap<String, Player> players;
    private GameState state;
    private String hostPlayerId;
    private final Random random;
    private final GameNotificationService notificationService;

    private long startTimeMillis;
    private static final long GAME_DURATION_LIMIT_MILLIS = TimeUnit.MINUTES.toMillis(5);

    public Game(String gameCode, GameNotificationService notificationService) {
        this.gameCode = gameCode;
        this.map = new Map(33, 33);
        this.players = new ConcurrentHashMap<>();
        this.state = GameState.WAITING;
        this.hostPlayerId = null;
        this.random = new Random();
        this.notificationService = notificationService;
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
        map.placePlayer(player);

        if (hostPlayerId == null) {
            hostPlayerId = player.getId();
        }
    }

    public void removePlayer(String playerId) {
        Player removed = players.remove(playerId);
        if (removed != null) {
            map.setCell(removed.getX(), removed.getY(), '.');
            if (playerId.equals(hostPlayerId) && !players.isEmpty()) {
                hostPlayerId = players.values().iterator().next().getId();
            }
        }
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }

    public Optional<Player> getPlayerById(String playerId) {
        return Optional.ofNullable(players.get(playerId));
    }

    public synchronized void processPlayerAction(String playerId, GameDTOs.PlayerAction action) {
        Player player = players.get(playerId);
        if (player != null && state == GameState.IN_PROGRESS) {
            switch(action) {
                case MOVE_UP:
                    player.move(0, -1, this);
                    break;
                case MOVE_DOWN:
                    player.move(0, 1, this);
                    break;
                case MOVE_LEFT:
                    player.move(-1, 0, this);
                    break;
                case MOVE_RIGHT:
                    player.move(1, 0, this);
                    break;
                case USE_POWERUP:
                    if (player instanceof Survivor survivor) {
                        survivor.usePowerUp();
                    }
                    break;
            }

            checkEscape(player);
            checkInfection();
            checkGameConditions();

            notificationService.notifyGameUpdate(this);
        }
    }

    private void checkInfection() {
        players.values().stream()
                .filter(Infected.class::isInstance)
                .map(Infected.class::cast)
                .forEach(infected -> players.values().stream()
                        .filter(Survivor.class::isInstance)
                        .map(Survivor.class::cast)
                        .filter(s -> isAdjacent(infected, s))
                        .forEach(s -> infected.infect(s, this)));
    }

    private void checkEscape(Player player) {
        if (player instanceof Survivor && player.getX() == 12 && player.getY() == 32) {
            logger.info("Un jugador ha escapado del juego");
            removePlayer(player.getId());
        }
    }

    private void checkGameConditions() {
        boolean allSurvivorsGone = players.values().stream()
                .noneMatch(Survivor.class::isInstance);

        boolean timeExpired = System.currentTimeMillis() - startTimeMillis >= GAME_DURATION_LIMIT_MILLIS;

        if (allSurvivorsGone || timeExpired) {
            endGame();
            notificationService.notifyGameUpdate(this);
        }
    }

    private boolean isAdjacent(Player p1, Player p2) {
        return Math.abs(p1.getX() - p2.getX()) <= 1 &&
                Math.abs(p1.getY() - p2.getY()) <= 1;
    }

    public boolean canStart() {
        return players.size() >= 1 && state == GameState.WAITING;
    }

    public void startGame() {
        if (!canStart()) return;

        state = GameState.IN_PROGRESS;
        startTimeMillis = System.currentTimeMillis();

        resetPlayerPositions();
        placePlayersInValidPositions();
        generatePowerUps();
        updateMapWithPlayers();
    }

    public long getRemainingTimeMillis() {
        long elapsed = System.currentTimeMillis() - startTimeMillis;
        return Math.max(0, GAME_DURATION_LIMIT_MILLIS - elapsed);
    }

    private void generatePowerUps() {
        int powerUpCount = players.size() * 2;

        for (int i = 0; i < powerUpCount; i++) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 100) {
                int x = random.nextInt(map.getWidth() - 2) + 1;
                int y = random.nextInt(map.getHeight() - 2) + 1;

                if (map.isWalkable(x, y) && !isPlayerAtPosition(x, y)) {
                    map.setCell(x, y, 'P');
                    placed = true;
                }
                attempts++;
            }
        }
    }

    public void collectPowerUp(int x, int y, String playerId) {
        if (map.getCell(x, y) == 'P') {
            getPlayerById(playerId).ifPresent(player -> {
                if (player instanceof Survivor survivor) {
                    map.setCell(x, y, '.');
                    survivor.collectPowerUp();
                    notificationService.notifyGameUpdate(this);
                }
            });
        }
    }

    private void resetPlayerPositions() {
        players.values().forEach(p -> map.setCell(p.getX(), p.getY(), '.'));
    }

    private void placePlayersInValidPositions() {
        players.values().forEach(player -> {
            boolean positionFound = false;
            int attempts = 0;
            while (!positionFound && attempts < 100) {
                int x = random.nextInt(map.getWidth() - 2) + 1;
                int y = random.nextInt(map.getHeight() - 2) + 1;

                if (map.isWalkable(x, y) && !isPlayerAtPosition(x, y)) {
                    player.setPosition(x, y);
                    positionFound = true;
                }
                attempts++;
            }
        });
    }

    private boolean isPlayerAtPosition(int x, int y) {
        return players.values().stream()
                .anyMatch(p -> p.getX() == x && p.getY() == y);
    }

    private void updateMapWithPlayers() {
        players.values().forEach(player ->
                map.setCell(player.getX(), player.getY(), player.getSymbol())
        );
    }

    public void endGame() {
        state = GameState.FINISHED;
    }

    // Getters
    public String getGameCode() { return gameCode; }
    public GameState getState() { return state; }
    public Map getMap() { return map; }
    public String getHostPlayerId() { return hostPlayerId; }
    public void setHostPlayerId(String hostPlayerId) { this.hostPlayerId = hostPlayerId; }

    public enum GameState {
        WAITING("waiting"),
        IN_PROGRESS("in_progress"),
        FINISHED("finished");

        private final String stateName;

        GameState(String stateName) {
            this.stateName = stateName;
        }

        @Override
        public String toString() {
            return stateName;
        }
    }
}