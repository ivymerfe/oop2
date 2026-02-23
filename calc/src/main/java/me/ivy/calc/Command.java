package me.ivy.calc;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public abstract class Command {
    public abstract String execute(ExecutionContext context, List<Object> args) throws CommandException;

    protected String requireStringArgument(List<Object> args, int index, String expected) throws CommandArgumentsException {
        if (index >= args.size()) {
            throw new CommandArgumentsException("Expected argument: " + expected);
        }
        return String.valueOf(args.get(index));
    }

    protected OptionalDouble resolveNumericToken(ExecutionContext context, String token) {
        Map<String, Double> vars = context.variables();
        if (vars.containsKey(token)) {
            return OptionalDouble.of(vars.get(token));
        }
        try {
            return OptionalDouble.of(Double.parseDouble(token));
        } catch (NumberFormatException ex) {
            return OptionalDouble.empty();
        }
    }
}
