package ECI.ARSW.Model.Game.dto;

import ECI.ARSW.Model.Player.Player; // Import necesario si vas a mantener PlayerHandler
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class PlayerInfo {
    private final String id;
    private final String name;
    private final boolean ready;
    private final boolean infected;

    public PlayerInfo(String id, String name, boolean infected, boolean ready) {
        this.id = id;
        this.name = name;
        this.infected = infected;
        this.ready = ready;
    }
}