package me.ivy.basic_commands;

import me.ivy.calc.ArgumentType;
import me.ivy.calc.Command;
import me.ivy.calc.CommandArgumentsException;
import me.ivy.calc.CommandSignature;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

@CommandName("DEFINE")
public class DefineCommand extends Command {
    @CommandSignature(count = 2, types = {ArgumentType.IDENTIFIER, ArgumentType.NUMBER})
    public void execute(ExecutionContext context, String name, Double value) {
        context.variables().put(name, value);
    }

    @CommandSignature(count = 2, types = {ArgumentType.IDENTIFIER, ArgumentType.IDENTIFIER})
    public void execute(ExecutionContext context, String name, String sourceVariable) throws CommandArgumentsException {
        if (!context.variables().containsKey(sourceVariable)) {
            throw new CommandArgumentsException("Unknown value: " + sourceVariable);
        }
        context.variables().put(name, context.variables().get(sourceVariable));
    }
}
