package me.ivy.calc;

/**
 * Indicates that requested command name is not registered in command factory.
 */
public class UnknownCommandException extends CommandException {
    /**
     * Creates exception with message.
     *
     * @param message error message
     */
    public UnknownCommandException(String message) {
        super(message);
    }
}