package edu.eci.arsw.service;

import edu.eci.arsw.model.Game;
import edu.eci.arsw.model.dto.GameDTOs;
import edu.eci.arsw.model.player.Infected;
import edu.eci.arsw.model.player.Player;
import edu.eci.arsw.model.player.Survivor;
import edu.eci.arsw.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class GameService {
    // Constantes aÃ±adidas
    private static final double SURVIVOR_PROBABILITY = 0.7; // 70% de probabilidad de ser Survivor por defecto
    private static final int MAX_INITIAL_POSITION = 30; // MÃ¡xima posiciÃ³n inicial en el mapa

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameNotificationService notificationService;

    private final Random random = new Random();

    public GameDTOs.GameInfo handleJoinGame(GameDTOs.PlayerJoinRequest joinRequest) {
        validateJoinRequest(joinRequest);

        Game game = gameRepository.findOrCreateGame(joinRequest.getGameCode());
        Player player = createPlayerWithDistribution(joinRequest, game);
        game.addPlayer(player);
        notificationService.notifyGameUpdate(game);

        return new GameDTOs.GameInfo(game.getGameCode(), game.getState().toString());
    }

    public Player createPlayerWithDistribution(GameDTOs.PlayerJoinRequest joinRequest, Game game) {
        int x = random.nextInt(MAX_INITIAL_POSITION) + 1;
        int y = random.nextInt(MAX_INITIAL_POSITION) + 1;

        // Calcular distribuciÃ³n basada en jugadores existentes
        long infectedCount = game.getPlayers().stream()
                .filter(p -> p instanceof Infected)
                .count();
        long totalPlayers = game.getPlayers().size();

        // Determinar tipo basado en la distribuciÃ³n actual
        boolean isInfected;

        if (totalPlayers == 0) {
            // Primer jugador - 20% de chance de ser infectado
            isInfected = random.nextDouble() < 0.2;
        } else {
            double currentRatio = (double) infectedCount / totalPlayers;
            double targetRatio = 0.25; // 25% infectados mÃ¡ximo

            if (currentRatio < targetRatio) {
                // Necesitamos mÃ¡s infectados para alcanzar la proporciÃ³n
                isInfected = random.nextDouble() < (targetRatio - currentRatio);
            } else {
                // Tenemos suficientes infectados
                isInfected = random.nextDouble() < 0.1;
            }
        }

        return createPlayer(joinRequest, x, y, !isInfected);
    }

    private Player createRandomPlayer(GameDTOs.PlayerJoinRequest joinRequest) {
        int x = random.nextInt(MAX_INITIAL_POSITION);
        int y = random.nextInt(MAX_INITIAL_POSITION);
        boolean isSurvivor = random.nextDouble() < SURVIVOR_PROBABILITY;

        return createPlayer(joinRequest, x, y, isSurvivor);
    }

    public Player createPlayer(GameDTOs.PlayerJoinRequest joinRequest, int x, int y, boolean isSurvivor) {
        validateJoinRequest(joinRequest);

        if (isSurvivor) {
            return new Survivor(joinRequest.getPlayerId(), x, y, joinRequest.getPlayerName());
        }
        return new Infected(joinRequest.getPlayerId(), x, y, joinRequest.getPlayerName());
    }

    private void validateJoinRequest(GameDTOs.PlayerJoinRequest joinRequest) {
        if (joinRequest == null || joinRequest.getPlayerId() == null || joinRequest.getPlayerName() == null) {
            throw new IllegalArgumentException("PlayerJoinRequest invÃ¡lido");
        }
    }
}
