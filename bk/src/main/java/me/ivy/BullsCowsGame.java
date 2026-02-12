package me.ivy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Реализация логики игры «Быки и коровы».
 */
public class BullsCowsGame implements IBullsCowsGame {
    private static final Logger logger = LogManager.getLogger(BullsCowsGame.class);

    List<Character> digits = Arrays.asList('0','1','2','3','4','5','6','7','8','9');
    int secretLength;

    public void generateSecretNumber(int length) {
        if (length < 1 || length > 10) {
            throw new IllegalArgumentException("secret length must be between 1 and 10");
        }
        logger.info("generateSecretNumber: length={}", length);
        this.secretLength = length;
        Collections.shuffle(digits);
        logger.debug("generateSecretNumber: secret={}", getSecretNumber());
    }

    public String getSecretNumber() {
        char[] tmp = new char[this.secretLength];
        for (int i = 0; i < this.secretLength; i++) {
            tmp[i] = digits.get(i);
        }
        return String.valueOf(tmp);
    }

    public GuessResult tryGuess(String guess) {
        if (guess.length() != secretLength) {
            throw new IllegalArgumentException("Guess length must be exactly " + secretLength);
        }
        logger.debug("tryGuess: guess='{}'", guess);
        int bulls = 0;
        int cows = 0;
        for (int i = 0; i < secretLength; i++) {
            int idx = digits.indexOf(guess.charAt(i));
            if (idx == i) bulls += 1;
            else if (idx < secretLength) cows += 1;
        }
        logger.debug("tryGuess: result bulls={}, cows={}", bulls, cows);
        return new GuessResult(bulls, cows);
    }
}
