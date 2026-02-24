package me.ivy.calc;

/**
 * Base checked exception for all command-related errors.
 */
public class CommandException extends Exception {
    /**
     * Creates exception with message.
     *
     * @param message error message
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Creates exception with message and cause.
     *
     * @param message error message
     * @param cause root cause
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}