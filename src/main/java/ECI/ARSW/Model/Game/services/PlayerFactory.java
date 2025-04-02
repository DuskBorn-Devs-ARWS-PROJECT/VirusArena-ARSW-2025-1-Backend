package ECI.ARSW.Model.Game.services;

import ECI.ARSW.Model.Player.*;
import org.springframework.stereotype.Service;

@Service
public class PlayerFactory {
    public Player createPlayer(String type, int x, int y, String name) {
        return switch (type.toLowerCase()) {
            case "infected" -> new Infected(x, y, name);
            case "survivor" -> new Survivor(x, y, name);
            default -> throw new IllegalArgumentException("Invalid player type");
        };
    }
}