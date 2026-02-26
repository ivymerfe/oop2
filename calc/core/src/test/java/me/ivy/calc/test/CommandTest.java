package me.ivy.calc.test;

import me.ivy.basic_commands.DefineCommand;
import me.ivy.basic_commands.PopCommand;
import me.ivy.basic_commands.PushCommand;
import me.ivy.basic_commands.SqrtCommand;
import me.ivy.binary_commands.AddCommand;
import me.ivy.binary_commands.DivideCommand;
import me.ivy.binary_commands.MultiplyCommand;
import me.ivy.binary_commands.SubtractCommand;
import me.ivy.calc.ExecutionContext;
import me.ivy.calc.Stack;
import me.ivy.calc.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        PushCommand push = new PushCommand();
        push.execute(ctx, 10.0);
        assertEquals(10.0, stack.pop());
    }

    @Test
    void testPopCommand() throws Exception {
        stack.push(7.0);
        PopCommand pop = new PopCommand();
        pop.execute(ctx);
        assertTrue(stack.isEmpty());
    }

    @Test
    void testDefineCommand() throws Exception {
        DefineCommand define = new DefineCommand();
        define.execute(ctx, "a", 2.5);
        assertEquals(2.5, vars.asMap().get("a"));
    }

    @Test
    void testSqrtCommand() throws Exception {
        stack.push(16.0);
        SqrtCommand sqrt = new SqrtCommand();
        sqrt.execute(ctx);
        assertEquals(4.0, stack.pop());
    }

    @Test
    void testBinaryCommandAdd() throws Exception {
        stack.push(2.0);
        stack.push(3.0);
        AddCommand add = new AddCommand();
        add.execute(ctx);
        assertEquals(5.0, stack.pop());
    }

    @Test
    void testBinaryCommandDivByZero() {
        stack.push(1.0);
        stack.push(0.0);
        DivideCommand div = new DivideCommand();
        Exception ex = assertThrows(Exception.class, () -> div.execute(ctx));
        assertTrue(ex.getMessage().toLowerCase().contains("zero"));
        assertEquals(0.0, stack.pop());
        assertEquals(1.0, stack.pop());
    }

    @Test
    void testPushCommandWithVariableToken() throws Exception {
        vars.asMap().put("n", 42.0);
        PushCommand push = new PushCommand();

        push.execute(ctx, "n");

        assertEquals(42.0, stack.pop());
    }

    @Test
    void testDefineCommandFromVariableReference() throws Exception {
        vars.asMap().put("a", 5.5);
        DefineCommand define = new DefineCommand();

        define.execute(ctx, "b", "a");

        assertEquals(5.5, vars.asMap().get("b"));
    }

    @Test
    void testPopCommandOnEmptyStack() {
        PopCommand pop = new PopCommand();
        Exception ex = assertThrows(Exception.class, () -> pop.execute(ctx));
        assertTrue(ex.getMessage().toLowerCase().contains("empty"));
    }

    @Test
    void testSqrtCommandNegativeValue() {
        stack.push(-4.0);
        SqrtCommand sqrt = new SqrtCommand();

        Exception ex = assertThrows(Exception.class, () -> sqrt.execute(ctx));

        assertTrue(ex.getMessage().toLowerCase().contains("negative"));
        assertEquals(-4.0, stack.pop());
    }

    @Test
    void testBinarySubtraction() throws Exception {
        stack.push(10.0);
        stack.push(3.0);
        SubtractCommand sub = new SubtractCommand();

        sub.execute(ctx);

        assertEquals(7.0, stack.pop());
    }

    @Test
    void testBinaryMultiplication() throws Exception {
        stack.push(2.5);
        stack.push(4.0);
        MultiplyCommand mul = new MultiplyCommand();

        mul.execute(ctx);

        assertEquals(10.0, stack.pop());
    }

    @Test
    void testBinaryNotEnoughValues() {
        stack.push(1.0);
        AddCommand add = new AddCommand();

        Exception ex = assertThrows(Exception.class, () -> add.execute(ctx));

        assertTrue(ex.getMessage().toLowerCase().contains("empty"));
    }
}
