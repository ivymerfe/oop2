package me.ivy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Application started");
        IBullsCowsGame game = new BullsCowsGame();
        GameTui tui = new GameTui(System.in, System.out);
        GameHost host = new GameHost(game, tui);

        GameParameters defaults = new GameParameters();
        logger.info("Default parameters: {}", defaults);
        GameParameters params = tui.chooseGameParameters(defaults);
        logger.info("Selected parameters: {}", params);
        
        while (true) {
            logger.info("New game session started: {}", params);
            host.hostGame(params);
            logger.info("Game session finished");
            tui.printSeparator();
            boolean again = tui.askYesNo("Еще? (Y/n): ", true);
            logger.info("Replay choice: {}", again ? "YES" : "NO");
            if (!again) {
                logger.info("Exit by user");
                return;
            }
        }
    }
}