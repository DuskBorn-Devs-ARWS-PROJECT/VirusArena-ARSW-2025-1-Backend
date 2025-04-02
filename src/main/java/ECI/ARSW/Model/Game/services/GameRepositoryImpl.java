package ECI.ARSW.Model.Game.services;

import ECI.ARSW.Model.Game.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import ECI.ARSW.Model.Game.exceptions.GameException;

@Service
public class GameRepositoryImpl implements GameRepository {
    private final ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();

    @Override
    public Game findGameByCode(String gameCode) throws GameException {
        Game game = games.get(gameCode);
        if (game == null) {
            throw new GameException("Game not found");
        }
        return game;
    }

    @Override
    public void saveGame(Game game) {
        games.put(game.getGameCode(), game);
    }

    @Override
    public boolean containsGame(String gameCode) {
        return games.containsKey(gameCode);
    }
}