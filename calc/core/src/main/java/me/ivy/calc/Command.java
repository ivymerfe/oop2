package me.ivy.calc;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Base type for all calculator commands.
 * A command receives execution context and raw arguments and returns command output text.
 */
public abstract class Command {
    /**
     * Executes a command.
     *
     * @param context current execution context with stack and variables
     * @param args raw command arguments
     * @return command output text, or empty string when no output is produced
     * @throws CommandException when command fails
     */
    public abstract String execute(ExecutionContext context, List<Object> args) throws CommandException;

    /**
     * Returns a required argument as string.
     *
     * @param args argument list
     * @param index argument index
     * @param expected short description of expected value
     * @return argument converted to string
     * @throws CommandArgumentsException when argument is missing
     */
    protected String requireStringArgument(List<Object> args, int index, String expected) throws CommandArgumentsException {
        if (index >= args.size()) {
            throw new CommandArgumentsException("Expected argument: " + expected);
        }
        return String.valueOf(args.get(index));
    }

    /**
     * Resolves token to numeric value from variables map or direct number parsing.
     *
     * @param context execution context
     * @param token value token
     * @return resolved numeric value or empty optional if token cannot be resolved
     */
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
