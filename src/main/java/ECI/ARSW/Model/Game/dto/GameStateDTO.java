package ECI.ARSW.Model.Game.dto;

import ECI.ARSW.Model.Game.*;
import java.util.List;

public class GameStateDTO {
    private String gameCode;
    private GameState state;
    private char[][] map;
    private List<PlayerStateDTO> players;
    private List<PowerUpDTO> powerUps;

    // Getters y Setters
    public String getGameCode() { return gameCode; }
    public void setGameCode(String gameCode) { this.gameCode = gameCode; }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public char[][] getMap() { return map; }
    public void setMap(char[][] map) { this.map = map; }

    public List<PlayerStateDTO> getPlayers() { return players; }
    public void setPlayers(List<PlayerStateDTO> players) { this.players = players; }

    public List<PowerUpDTO> getPowerUps() { return powerUps; }
    public void setPowerUps(List<PowerUpDTO> powerUps) { this.powerUps = powerUps; }
}