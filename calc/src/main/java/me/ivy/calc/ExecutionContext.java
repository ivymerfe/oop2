package me.ivy.calc;

import java.util.Map;

public class ExecutionContext {
    private final Stack stack;
    private final Map<String, Double> variables;

    public ExecutionContext(Stack stack, Map<String, Double> variables) {
        this.stack = stack;
        this.variables = variables;
    }

    public Stack stack() {
        return stack;
    }

    public Map<String, Double> variables() {
        return variables;
    }
}