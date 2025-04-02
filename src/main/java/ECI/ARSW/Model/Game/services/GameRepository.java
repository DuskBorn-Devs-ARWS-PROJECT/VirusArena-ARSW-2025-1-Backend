package ECI.ARSW.Model.Game.services;

import ECI.ARSW.Model.Game.*;
import java.util.concurrent.ConcurrentHashMap;
import ECI.ARSW.Model.Game.exceptions.GameException;

public interface GameRepository {
    Game findGameByCode(String gameCode) throws GameException;
    void saveGame(Game game);
    boolean containsGame(String gameCode);
}