package me.ivy.calc;

import java.util.Map;

/**
 * Immutable holder of calculator runtime objects used by commands.
 */
public class ExecutionContext {
    private final Stack stack;
    private final Map<String, Double> variables;

    /**
     * Creates execution context.
     *
     * @param stack calculator stack
     * @param variables calculator variables map
     */
    public ExecutionContext(Stack stack, Map<String, Double> variables) {
        this.stack = stack;
        this.variables = variables;
    }

    /**
     * Returns stack instance.
     *
     * @return stack
     */
    public Stack stack() {
        return stack;
    }

    /**
     * Returns variables map.
     *
     * @return variables map
     */
    public Map<String, Double> variables() {
        return variables;
    }
}