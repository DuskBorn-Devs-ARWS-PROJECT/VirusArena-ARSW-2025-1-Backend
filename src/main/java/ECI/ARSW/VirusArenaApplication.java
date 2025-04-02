package ECI.ARSW;

import ECI.ARSW.Model.Game.*;
import ECI.ARSW.Model.Player.*;
import ECI.ARSW.Model.PowerUp.*;
import ECI.ARSW.Model.Game.services.*;
import ECI.ARSW.Model.Game.dto.*;
import ECI.ARSW.Model.Game.exceptions.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootApplication
public class VirusArenaApplication implements CommandLineRunner {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameNotificationService notificationService;

    public VirusArenaApplication(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.notificationService = new GameNotificationService(messagingTemplate);
    }

    public static void main(String[] args) {
        SpringApplication.run(VirusArenaApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length > 0 && args[0].equals("--test-data")) {
            initializeTestData();
        }
    }

    private void initializeTestData() {
        try {
            // Create a test game
            Game testGame = new Game("test-game", 33, 33, notificationService);

            // Add players at valid positions
            Survivor survivor1 = new Survivor(5, 5, "Survivor1");
            Survivor survivor2 = new Survivor(10, 10, "Survivor2");
            Infected infected1 = new Infected(15, 15, "Infected1");
            Infected infected2 = new Infected(20, 20, "Infected2");

            testGame.addPlayer(survivor1);
            testGame.addPlayer(survivor2);
            testGame.addPlayer(infected1);
            testGame.addPlayer(infected2);

            // Add power-ups
            StaminaPowerUp powerUp1 = new StaminaPowerUp(7, 7);
            StaminaPowerUp powerUp2 = new StaminaPowerUp(12, 12);
            StaminaPowerUp powerUp3 = new StaminaPowerUp(18, 18);

            testGame.addPowerUp(powerUp1);
            testGame.spawnRandomPowerUp(powerUp2);
            testGame.spawnRandomPowerUp(powerUp3);

            // Mark players as ready for testing game start
            testGame.togglePlayerReady(survivor1.getId());
            testGame.togglePlayerReady(survivor2.getId());
            testGame.togglePlayerReady(infected1.getId());
            testGame.togglePlayerReady(infected2.getId());

            System.out.println("Test game initialized with code: test-game");
            System.out.println("Players: Survivor1, Survivor2, Infected1, Infected2");
            System.out.println("Power-ups placed at fixed and random positions");

        } catch (Exception e) {
            System.err.println("Failed to initialize test data: " + e.getMessage());
        }
    }

    @Bean
    public GameNotificationService gameNotificationService(SimpMessagingTemplate messagingTemplate) {
        return new GameNotificationService(messagingTemplate);
    }
}