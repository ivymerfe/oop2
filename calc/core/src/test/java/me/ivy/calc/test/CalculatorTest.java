package me.ivy.calc.test;

import me.ivy.calc.Calculator;
import me.ivy.calc.Command;
import me.ivy.calc.ExecutionContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTest {
    @Test
    void testPushAndPop() {
        Calculator calc = new Calculator();
        calc.execute("PUSH 5\nPUSH 3\nPOP");
        assertEquals("5.0", calc.getStackItems().get(0));
        assertEquals(1, calc.getStackItems().size());
    }

    @Test
    void testDefineAndPush() {
        Calculator calc = new Calculator();
        calc.execute("DEFINE x 7\nPUSH x");
        assertEquals("7.0", calc.getStackItems().get(0));
        assertEquals("x = 7.0", calc.getVariableItems().get(0));
    }

    @Test
    void testArithmetic() {
        Calculator calc = new Calculator();
        calc.execute("PUSH 2\nPUSH 3\n+");
        assertEquals("5.0", calc.getStackItems().get(0));
        calc.execute("PUSH 4\n*");
        assertEquals("20.0", calc.getStackItems().get(0));
        calc.execute("PUSH 2\n/");
        assertEquals("10.0", calc.getStackItems().get(0));
        calc.execute("PUSH 1\n-");
        assertEquals("9.0", calc.getStackItems().get(0));
    }

    @Test
    void testSqrt() {
        Calculator calc = new Calculator();
        calc.execute("PUSH 9\nSQRT");
        assertEquals("3.0", calc.getStackItems().get(0));
    }

    @Test
    void testErrorHandling() {
        Calculator calc = new Calculator();
        String out = calc.execute("POP");
        assertTrue(out.startsWith("ERR"));
        out = calc.execute("SQRT");
        assertTrue(out.startsWith("ERR"));
        out = calc.execute("PUSH a");
        assertTrue(out.startsWith("ERR"));
    }

    @Test
    void testCommentsAndSemicolonCommands() {
        Calculator calc = new Calculator();
        calc.execute("# comment\nPUSH 5; PUSH 2; -   # trailing comment");
        assertEquals(1, calc.getStackItems().size());
        assertEquals("3.0", calc.getStackItems().get(0));
    }

    @Test
    void testDivisionByZeroKeepsStackState() {
        Calculator calc = new Calculator();
        String out = calc.execute("PUSH 7\nPUSH 0\n/");
        assertTrue(out.startsWith("ERR"));
        assertEquals(List.of("0.0", "7.0"), calc.getStackItems());
    }

    @Test
    void testSqrtNegativeKeepsValueOnStack() {
        Calculator calc = new Calculator();
        String out = calc.execute("PUSH -9\nSQRT");
        assertTrue(out.startsWith("ERR"));
        assertEquals(List.of("-9.0"), calc.getStackItems());
    }

    @Test
    void testUnknownCommandReturnsError() {
        Calculator calc = new Calculator();
        String out = calc.execute("UNKNOWN_CMD");
        assertTrue(out.startsWith("ERR"));
    }

    @Test
    void testExecuteNullProgramReturnsEmpty() {
        Calculator calc = new Calculator();
        assertEquals("", calc.execute(null));
        assertTrue(calc.getStackItems().isEmpty());
    }

    @Test
    void testProgrammaticExecuteCommandApi() {
        Calculator calc = new Calculator();
        assertEquals("", calc.executeCommand("PUSH", "11"));
        assertEquals("", calc.executeCommand("PUSH", "4"));
        assertEquals("", calc.executeCommand("-"));
        assertEquals(List.of("7.0"), calc.getStackItems());
    }

    @Test
    void testProgrammaticRegisterCustomCommand() {
        Calculator calc = new Calculator();
        calc.registerCommand("DOUBLE", new DoubleTopCommand());

        calc.execute("PUSH 6");
        String out = calc.executeCommand("DOUBLE");

        assertEquals("", out);
        assertEquals(List.of("12.0"), calc.getStackItems());
    }

    private static class DoubleTopCommand extends Command {
        @Override
        public String execute(ExecutionContext context, List<Object> args) {
            double value = context.stack().pop();
            context.stack().push(value * 2);
            return "";
        }
    }
}
