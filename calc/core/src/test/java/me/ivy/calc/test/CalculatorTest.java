package me.ivy.calc.test;

import me.ivy.calc.Command;
import me.ivy.calc.CommandException;
import me.ivy.calc.CommandFactory;
import me.ivy.calc.CommandName;
import me.ivy.calc.CommandSignature;
import me.ivy.calc.Calculator;
import me.ivy.calc.ExecutionContext;
import me.ivy.calc.UnknownCommandException;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CalculatorTest {
    @Test
    void testPushAndPop() {
        Calculator calc = new Calculator(new CommandFactory());
        assertDoesNotThrow(() -> calc.execute("PUSH 5\nPUSH 3\nPOP"));
        assertEquals("5.0", calc.getStackItems().get(0));
        assertEquals(1, calc.getStackItems().size());
    }

    @Test
    void testDefineAndPush() {
        Calculator calc = new Calculator(new CommandFactory());
        assertDoesNotThrow(() -> calc.execute("DEFINE x 7\nPUSH x"));
        assertEquals("7.0", calc.getStackItems().get(0));
        assertEquals("x = 7.0", calc.getVariableItems().get(0));
    }

    @Test
    void testArithmetic() {
        Calculator calc = new Calculator(new CommandFactory());
        assertDoesNotThrow(() -> calc.execute("PUSH 2\nPUSH 3\n+"));
        assertEquals("5.0", calc.getStackItems().get(0));
        assertDoesNotThrow(() ->calc.execute("PUSH 4\n*"));
        assertEquals("20.0", calc.getStackItems().get(0));
        assertDoesNotThrow(() ->calc.execute("PUSH 2\n/"));
        assertEquals("10.0", calc.getStackItems().get(0));
        assertDoesNotThrow(() ->calc.execute("PUSH 1\n-"));
        assertEquals("9.0", calc.getStackItems().get(0));
    }

    @Test
    void testSqrt() {
        Calculator calc = new Calculator(new CommandFactory());
        assertDoesNotThrow(() -> calc.execute("PUSH 9\nSQRT"));
        assertEquals("3.0", calc.getStackItems().get(0));
    }

    @Test
    void testErrorHandling() {
        Calculator calc = new Calculator(new CommandFactory());
        assertThrows(CommandException.class, () -> calc.execute("POP"));
        assertThrows(CommandException.class, () -> calc.execute("SQRT"));
        assertThrows(CommandException.class, () -> calc.execute("PUSH a"));
    }

    @Test
    void testCommentsAndSemicolonCommands() {
        Calculator calc = new Calculator(new CommandFactory());
        assertDoesNotThrow(() -> calc.execute("# comment\nPUSH 5; PUSH 2; -   # trailing comment"));
        assertEquals(1, calc.getStackItems().size());
        assertEquals("3.0", calc.getStackItems().get(0));
    }

    @Test
    void testDivisionByZeroKeepsStackState() {
        Calculator calc = new Calculator(new CommandFactory());
        assertThrows(CommandException.class, () -> calc.execute("PUSH 7\nPUSH 0\n/"));
        assertEquals(List.of("0.0", "7.0"), calc.getStackItems());
    }

    @Test
    void testSqrtNegativeKeepsValueOnStack() {
        Calculator calc = new Calculator(new CommandFactory());
        assertThrows(CommandException.class, () -> calc.execute("PUSH -9\nSQRT"));
        assertEquals(List.of("-9.0"), calc.getStackItems());
    }

    @Test
    void testUnknownCommandReturnsError() {
        Calculator calc = new Calculator(new CommandFactory());
        assertThrows(UnknownCommandException.class, () -> calc.execute("UNKNOWN_CMD"));
    }

    @Test
    void testProgrammaticExecuteCommandApi() {
        Calculator calc = new Calculator(new CommandFactory());
        assertDoesNotThrow(() -> {
            calc.executeCommand("PUSH", "11");
            calc.executeCommand("PUSH", "4");
            calc.executeCommand("-");
        });
        assertEquals(List.of("7.0"), calc.getStackItems());
    }

    @Test
    void testProgrammaticRegisterCustomCommand() {
        Calculator calc = new Calculator(new CommandFactory());
        calc.registerCommand("DOUBLE", DoubleTopCommand.class);

        assertDoesNotThrow(() -> {
            calc.execute("PUSH 6");
            calc.executeCommand("DOUBLE");
        });

        assertEquals(List.of("12.0"), calc.getStackItems());
    }

    @CommandName("DOUBLE")
    public static class DoubleTopCommand extends Command {
        @CommandSignature(count = 0, types = {}, requiredStackSize = 1)
        public void execute(ExecutionContext context) {
            double value = context.stack().pop();
            context.stack().push(value * 2);
        }
    }
}
