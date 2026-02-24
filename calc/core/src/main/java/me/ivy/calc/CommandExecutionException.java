package me.ivy.calc;

/**
 * Indicates failure during command execution logic.
 */
public class CommandExecutionException extends CommandException {
    /**
     * Creates exception with message.
     *
     * @param message error message
     */
    public CommandExecutionException(String message) {
        super(message);
    }
}