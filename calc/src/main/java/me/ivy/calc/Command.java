package me.ivy.calc;

public abstract class Command {
    protected final Stack stack;
    protected final Variables variables;

    public Command(Stack stack, Variables variables) {
        this.stack = stack;
        this.variables = variables;
    }

    public abstract void execute(String[] args) throws CommandException;

    public static class CommandException extends Exception {
        public CommandException(String message) {
            super(message);
        }
    }
}
