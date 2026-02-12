package me.ivy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class BullsCowsGameTest {

    @Test
    void generateSecretNumberRejectsLengthBelowRange() {
        BullsCowsGame game = new BullsCowsGame();

        assertThrows(IllegalArgumentException.class, () -> game.generateSecretNumber(0));
    }

    @Test
    void generateSecretNumberRejectsLengthAboveRange() {
        BullsCowsGame game = new BullsCowsGame();

        assertThrows(IllegalArgumentException.class, () -> game.generateSecretNumber(11));
    }

    @Test
    void generateSecretNumberCreatesUniqueDigitsWithRequestedLength() {
        BullsCowsGame game = new BullsCowsGame();

        game.generateSecretNumber(4);
        String secret = game.getSecretNumber();

        assertEquals(4, secret.length());
        assertEquals(4, secret.chars().distinct().count());
        assertTrue(secret.chars().allMatch(Character::isDigit));
    }

    @Test
    void tryGuessCountsBullsAndCows() {
        BullsCowsGame game = new BullsCowsGame();
        game.secretLength = 4;
        game.digits = Arrays.asList('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');

        GuessResult result = game.tryGuess("1325");

        assertEquals(1, result.getBulls());
        assertEquals(2, result.getCows());
    }

    @Test
    void tryGuessThrowsWhenLengthIsInvalid() {
        BullsCowsGame game = new BullsCowsGame();
        game.secretLength = 4;
        game.digits = Arrays.asList('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');

        assertThrows(IllegalArgumentException.class, () -> game.tryGuess("123"));
    }
}
