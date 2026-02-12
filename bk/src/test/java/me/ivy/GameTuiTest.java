package me.ivy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class GameTuiTest {

    @Test
    void askYesNoReturnsDefaultOnEmptyInput() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GameTui tui = createTui("\n", out);

        boolean result = tui.askYesNo("Q: ", false);

        assertFalse(result);
    }

    @Test
    void askYesNoAcceptsRussianYes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GameTui tui = createTui("да\n", out);

        boolean result = tui.askYesNo("Q: ", false);

        assertTrue(result);
    }

    @Test
    void askIntRepeatsUntilValidInput() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GameTui tui = createTui("abc\n11\n5\n", out);

        int value = tui.askInt("Число", 4, 1, 10);

        assertEquals(5, value);
        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Нужно целое число."));
        assertTrue(output.contains("Допустимый диапазон: 1..10"));
    }

    @Test
    void chooseGameParametersReturnsDefaultsWhenUserSkipsChanges() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GameTui tui = createTui("\n", out);
        GameParameters defaults = new GameParameters();

        GameParameters selected = tui.chooseGameParameters(defaults);

        assertSame(defaults, selected);
    }

    @Test
    void chooseGameParametersReadsCustomValues() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GameTui tui = createTui("y\n6\n20\n45\n", out);
        GameParameters defaults = new GameParameters();

        GameParameters selected = tui.chooseGameParameters(defaults);

        assertEquals(6, selected.secretLength);
        assertEquals(20, selected.maxAttempts);
        assertEquals(45, selected.timeToGuess);
    }

    private GameTui createTui(String input, ByteArrayOutputStream outputStream) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        PrintStream out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);
        return new GameTui(in, out);
    }
}
