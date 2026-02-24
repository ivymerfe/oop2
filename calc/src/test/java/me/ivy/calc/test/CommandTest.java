package me.ivy.calc.test;

import me.ivy.calc.*;
import me.ivy.basic_commands.*;
import me.ivy.binary_commands.BinaryCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {
    private ExecutionContext ctx;
    private Stack stack;
    private Variables vars;

    @BeforeEach
    void setup() {
        stack = new Stack();
        vars = new Variables();
        ctx = new ExecutionContext(stack, vars.asMap());
    }

    @Test
    void testPushCommand() throws Exception {
        Command push = new PushCommand();
        push.execute(ctx, List.of("10"));
        assertEquals(10.0, stack.pop());
    }

    @Test
    void testPopCommand() throws Exception {
        stack.push(7.0);
        Command pop = new PopCommand();
        pop.execute(ctx, List.of());
        assertTrue(stack.isEmpty());
    }

    @Test
    void testDefineCommand() throws Exception {
        Command define = new DefineCommand();
        define.execute(ctx, List.of("a", "2.5"));
        assertEquals(2.5, vars.asMap().get("a"));
    }

    @Test
    void testSqrtCommand() throws Exception {
        stack.push(16.0);
        Command sqrt = new SqrtCommand();
        sqrt.execute(ctx, List.of());
        assertEquals(4.0, stack.pop());
    }

    @Test
    void testBinaryCommandAdd() throws Exception {
        stack.push(2.0);
        stack.push(3.0);
        Command add = new BinaryCommand("+");
        add.execute(ctx, List.of());
        assertEquals(5.0, stack.pop());
    }

    @Test
    void testBinaryCommandDivByZero() {
        stack.push(1.0);
        stack.push(0.0);
        Command div = new BinaryCommand("/");
        Exception ex = assertThrows(Exception.class, () -> div.execute(ctx, List.of()));
        assertTrue(ex.getMessage().toLowerCase().contains("zero"));
        assertEquals(0.0, stack.pop());
        assertEquals(1.0, stack.pop());
    }

    @Test
    void testPushCommandWithVariableToken() throws Exception {
        vars.asMap().put("n", 42.0);
        Command push = new PushCommand();

        push.execute(ctx, List.of("n"));

        assertEquals(42.0, stack.pop());
    }

    @Test
    void testPushCommandMissingArgument() {
        Command push = new PushCommand();
        Exception ex = assertThrows(Exception.class, () -> push.execute(ctx, List.of()));
        assertTrue(ex.getMessage().contains("Expected argument"));
    }

    @Test
    void testDefineCommandFromVariableReference() throws Exception {
        vars.asMap().put("a", 5.5);
        Command define = new DefineCommand();

        define.execute(ctx, List.of("b", "a"));

        assertEquals(5.5, vars.asMap().get("b"));
    }

    @Test
    void testDefineCommandMissingValue() {
        Command define = new DefineCommand();
        Exception ex = assertThrows(Exception.class, () -> define.execute(ctx, List.of("x")));
        assertTrue(ex.getMessage().contains("Expected argument"));
    }

    @Test
    void testPopCommandOnEmptyStack() {
        Command pop = new PopCommand();
        Exception ex = assertThrows(Exception.class, () -> pop.execute(ctx, List.of()));
        assertTrue(ex.getMessage().contains("empty stack"));
    }

    @Test
    void testSqrtCommandNegativeValue() {
        stack.push(-4.0);
        Command sqrt = new SqrtCommand();

        Exception ex = assertThrows(Exception.class, () -> sqrt.execute(ctx, List.of()));

        assertTrue(ex.getMessage().toLowerCase().contains("negative"));
        assertEquals(-4.0, stack.pop());
    }

    @Test
    void testBinarySubtraction() throws Exception {
        stack.push(10.0);
        stack.push(3.0);
        Command sub = new BinaryCommand("-");

        sub.execute(ctx, List.of());

        assertEquals(7.0, stack.pop());
    }

    @Test
    void testBinaryMultiplication() throws Exception {
        stack.push(2.5);
        stack.push(4.0);
        Command mul = new BinaryCommand("*");

        mul.execute(ctx, List.of());

        assertEquals(10.0, stack.pop());
    }

    @Test
    void testBinaryNotEnoughValues() {
        stack.push(1.0);
        Command add = new BinaryCommand("+");

        Exception ex = assertThrows(Exception.class, () -> add.execute(ctx, List.of()));

        assertTrue(ex.getMessage().contains("Not enough values"));
    }
}
