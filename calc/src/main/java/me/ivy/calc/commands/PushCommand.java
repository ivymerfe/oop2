package me.ivy.calc.commands;

import me.ivy.calc.Command;
import me.ivy.calc.Stack;
import me.ivy.calc.Variables;

import java.util.OptionalDouble;

public class PushCommand extends Command {
    public PushCommand(Stack stack, Variables variables) {
        super(stack, variables);
    }

    @Override
    public void execute(String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("PUSH requires a value");
        }
        OptionalDouble value = resolveValue(args[1]);
        if (value.isEmpty()) {
            throw new CommandException("Unknown value: " + args[1]);
        }
        stack.push(value.getAsDouble());
    }

    private OptionalDouble resolveValue(String token) {
        if (variables.contains(token)) {
            return OptionalDouble.of(variables.get(token));
        }
        try {
            return OptionalDouble.of(Double.parseDouble(token));
        } catch (NumberFormatException ex) {
            return OptionalDouble.empty();
        }
    }
}
