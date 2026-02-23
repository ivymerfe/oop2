package me.ivy.calc;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public abstract class Command {
    protected final ExecutionContext context;

    protected Command(ExecutionContext context) {
        this.context = context;
    }

    public abstract String execute(List<Object> args) throws CommandException;

    protected Stack stack() {
        return context.getStack();
    }

    protected Map<String, Double> variables() {
        return context.getVariables();
    }

    protected String requireStringArgument(List<Object> args, int index, String expected) throws CommandArgumentsException {
        if (index >= args.size()) {
            throw new CommandArgumentsException("Expected argument: " + expected);
        }
        return String.valueOf(args.get(index));
    }

    protected OptionalDouble resolveNumericToken(String token) {
        if (variables().containsKey(token)) {
            return OptionalDouble.of(variables().get(token));
        }
        try {
            return OptionalDouble.of(Double.parseDouble(token));
        } catch (NumberFormatException ex) {
            return OptionalDouble.empty();
        }
    }
}
