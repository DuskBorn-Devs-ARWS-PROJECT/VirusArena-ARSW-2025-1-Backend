package edu.eci.arsw.repository;

import edu.eci.arsw.model.Game;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.eci.arsw.service.GameNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GameRepository {
    private final ConcurrentMap<String, Game> games = new ConcurrentHashMap<>();
    private final GameNotificationService notificationService;

    // Inyectar el servicio en el repositorio
    @Autowired
    public GameRepository(GameNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public Game findOrCreateGame(String gameCode) {
        return games.computeIfAbsent(
                gameCode == null ? generateGameCode() : gameCode.toUpperCase(),
                code -> {
                    System.out.println("Creando nuevo juego: " + code);
                    return new Game(code, notificationService); // Pasar el servicio al crear el juego
                }
        );
    }

    // Debug: Método para ver juegos activos
    public void printActiveGames() {
        System.out.println("=== JUEGOS ACTIVOS ===");
        games.forEach((code, game) -> {
            System.out.println("Game: " + code +
                    ", Jugadores: " + game.getPlayers().size());
        });
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
        return "GAME" + (int)(Math.random() * 10000);
    }
}