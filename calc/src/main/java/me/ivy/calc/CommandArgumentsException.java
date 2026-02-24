package me.ivy.calc;

/**
 * Indicates invalid or missing command arguments.
 */
public class CommandArgumentsException extends CommandException {
    /**
     * Creates exception with message.
     *
     * @param message error message
     */
    public CommandArgumentsException(String message) {
        super(message);
    }
}