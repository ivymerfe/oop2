package me.ivy.basic_commands;

import me.ivy.calc.ArgumentType;
import me.ivy.calc.Command;
import me.ivy.calc.CommandArgumentsException;
import me.ivy.calc.CommandSignature;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

@CommandName("PUSH")
public class PushCommand extends Command {
    @CommandSignature(count = 1, types = {ArgumentType.NUMBER})
    public void execute(ExecutionContext context, Double value) {
        context.stack().push(value);
    }

    @CommandSignature(count = 1, types = {ArgumentType.IDENTIFIER})
    public void execute(ExecutionContext context, String variableName) throws CommandArgumentsException {
        if (!context.variables().containsKey(variableName)) {
            throw new CommandArgumentsException("Unknown value: " + variableName);
        }
        context.stack().push(context.variables().get(variableName));
    }
}
