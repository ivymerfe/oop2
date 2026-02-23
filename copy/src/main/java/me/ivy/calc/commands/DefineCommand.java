package me.ivy.calc.commands;

import me.ivy.calc.Command;
import me.ivy.calc.Stack;
import me.ivy.calc.Variables;

import java.util.OptionalDouble;

public class DefineCommand extends Command {
    public DefineCommand(Stack stack, Variables variables) {
        super(stack, variables);
    }

    @Override
    public void execute(String[] args) throws CommandException {
        if (args.length < 3) {
            throw new CommandException("DEFINE requires name and value");
        }
        String name = args[1];
        OptionalDouble value = resolveValue(args[2]);
        if (value.isEmpty()) {
            throw new CommandException("Unknown value: " + args[2]);
        }
        variables.define(name, value.getAsDouble());
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
