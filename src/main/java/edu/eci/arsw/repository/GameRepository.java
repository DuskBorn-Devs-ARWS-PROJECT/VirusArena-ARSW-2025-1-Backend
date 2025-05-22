package edu.eci.arsw.repository;

import edu.eci.arsw.model.Game;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.eci.arsw.service.GameNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GameRepository {
    // Usamos SecureRandom por razones de seguridad y para evitar advertencias de análisis estático
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ConcurrentMap<String, Game> games = new ConcurrentHashMap<>();
    private final GameNotificationService notificationService;
    private static final Logger logger = LoggerFactory.getLogger(GameRepository.class);

    @Autowired
    public GameRepository(GameNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public Game findOrCreateGame(String gameCode) {
        return games.computeIfAbsent(
                gameCode == null ? generateGameCode() : gameCode.toUpperCase(),
                code -> {
                    logger.info("Creando nuevo juego: {}", code);
                    return new Game(code, notificationService);
                }
        );
    }

    public void printActiveGames() {
        if (logger.isDebugEnabled()) {
            logger.debug("=== JUEGOS ACTIVOS ===");
            games.forEach((code, game) ->
                    logger.debug("Game: {}, Jugadores: {}", code, game.getPlayers().size())
            );
        }
    }

    public Game findGameByCode(String gameCode) {
        return games.get(gameCode);
    }

    public boolean removeGame(String gameCode) {
        return games.remove(gameCode) != null;
    }

    public boolean containsGame(String gameCode) {
        return games.containsKey(gameCode);
    }

    public int getActiveGamesCount() {
        return games.size();
    }

    private String generateGameCode() {
        return "GAME" + (1000 + RANDOM.nextInt(9000)); // Genera códigos de 4 dígitos
    }
}
