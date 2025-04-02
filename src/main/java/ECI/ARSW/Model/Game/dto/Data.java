package ECI.ARSW.Model.Game.dto;

import ECI.ARSW.Model.Player.*;
import ECI.ARSW.Model.PowerUp.*;
import ECI.ARSW.Model.Game.Game;
import java.util.List;

public class Data {
    private String gameCode;
    private GameState state;
    private List<Player> players;
    private List<PowerUp> powerUps;

    public Data(String gameCode, GameState state, List<Player> players, List<PowerUp> powerUps) {
        this.gameCode = gameCode;
        this.state = state;
        this.players = players;
        this.powerUps = powerUps;
    }

    // Getters
    public String getGameCode() { return gameCode; }
    public GameState getState() { return state; }
    public List<Player> getPlayers() { return players; }
    public List<PowerUp> getPowerUps() { return powerUps; }
}