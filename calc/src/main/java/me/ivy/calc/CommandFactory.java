package me.ivy.calc;

import me.ivy.calc.commands.*;

import java.util.Locale;

public class CommandFactory {
    private final Stack stack;
    private final Variables variables;

    public CommandFactory(Stack stack, Variables variables) {
        this.stack = stack;
        this.variables = variables;
    }

    public Command createCommand(String commandName) throws Command.CommandException {
        String cmd = commandName.toUpperCase(Locale.ROOT);
        return switch (cmd) {
            case "PUSH" -> new PushCommand(stack, variables);
            case "POP" -> new PopCommand(stack, variables);
            case "PRINT" -> new PrintCommand(stack, variables);
            case "DEFINE" -> new DefineCommand(stack, variables);
            case "SQRT" -> new SqrtCommand(stack, variables);
            case "+", "-", "*", "/" -> new BinaryCommand(stack, variables, cmd);
            default -> throw new Command.CommandException("Unknown command: " + commandName);
        };
    }
}
