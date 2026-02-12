package me.ivy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Координирует игровой процесс между логикой игры и текстовым интерфейсом.
 */
public class GameHost implements IGameHost {
    private static final Logger logger = LogManager.getLogger(GameHost.class);

    private final IBullsCowsGame game;
    private final GameTui tui;

    /**
     * @param game игровая логика
     * @param tui текстовый интерфейс
     */
    public GameHost(IBullsCowsGame game, GameTui tui) {
        this.game = game;
        this.tui = tui;
    }

    public void hostGame(GameParameters params) {
        logger.info("hostGame: start params={}", params);
        game.generateSecretNumber(params.secretLength);
        logger.debug("hostGame: secret generated (length={}): {}", params.secretLength, game.getSecretNumber());

        int attemptsUsed = 0;
        while (attemptsUsed < params.maxAttempts) {
            int attemptNo = attemptsUsed + 1;
            String status = String.format("Попытка %d/%d · Время:", attemptNo, params.maxAttempts);
            long attemptDeadlineMs = System.currentTimeMillis() + params.timeToGuess * 1000L;
            logger.info("Attempt {}/{} start (deadlineMs={})", attemptNo, params.maxAttempts, attemptDeadlineMs);

            String guess;
            while (true) {
                guess = tui.readLineWithDeadline("Какое же число?: ", status, attemptDeadlineMs);
                if (guess == null) {
                    logger.info("Attempt {}/{}: timeout (no input)", attemptNo, params.maxAttempts);
                    tui.println("Игра окончена: вышло время.");
                    tui.println("Загаданное число: " + game.getSecretNumber());
                    logger.info("hostGame: finish by timeout. secret={}", game.getSecretNumber());
                    return;
                }
                if (System.currentTimeMillis() > attemptDeadlineMs) {
                    logger.info("Attempt {}/{}: timeout (deadline exceeded after input)", attemptNo, params.maxAttempts);
                    tui.println("Игра окончена: вышло время.");
                    tui.println("Загаданное число: " + game.getSecretNumber());
                    logger.info("hostGame: finish by timeout. secret={}", game.getSecretNumber());
                    return;
                }
                if (guess.isEmpty()) {
                    logger.info("Attempt {}/{}: empty input", attemptNo, params.maxAttempts);
                    if (tui.confirmExit()) {
                        tui.println("Загаданное число: " + game.getSecretNumber());
                        logger.info("hostGame: user exit confirmed. secret={}", game.getSecretNumber());
                        return;
                    }
                    logger.info("hostGame: user chose to continue after empty input");
                    continue;
                }
                if (guess.length() != params.secretLength) {
                    logger.info("Attempt {}/{}: invalid length {} (expected {})", attemptNo, params.maxAttempts, guess.length(), params.secretLength);
                    tui.println(String.format("Надо %d цифр", params.secretLength));
                    continue;
                }
                if (!guess.matches("\\d+")) {
                    logger.info("Attempt {}/{}: invalid chars in guess='{}'", attemptNo, params.maxAttempts, guess);
                    tui.println("Только цифры");
                    continue;
                }
                if (guess.chars().distinct().count() != params.secretLength) {
                    logger.info("Attempt {}/{}: repeated digits in guess='{}'", attemptNo, params.maxAttempts, guess);
                    tui.println("Повторяющиеся цифры не допускаются");
                    continue;
                }
                break;
            }

            logger.info("Attempt {}/{}: guess accepted='{}'", attemptNo, params.maxAttempts, guess);
            GuessResult result = game.tryGuess(guess);
            attemptsUsed++;
            logger.info("Attempt {}/{}: result bulls={}, cows={}", attemptNo, params.maxAttempts, result.bulls, result.cows);

            if (result.bulls == params.secretLength) {
                tui.println("Ты смог!, " + game.getSecretNumber());
                logger.info("hostGame: win in {}/{} attempts. secret={}", attemptsUsed, params.maxAttempts, game.getSecretNumber());
                return;
            }

            tui.println(String.format("Быков: %d, Коров: %d", result.bulls, result.cows));
        }

        tui.println("Попытки закончились.");
        tui.println("Загаданное число: " + game.getSecretNumber());
        logger.info("hostGame: finish by attempts exhausted. secret={}", game.getSecretNumber());
    }
}
