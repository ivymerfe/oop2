package me.ivy.basic_commands;

import me.ivy.calc.Command;
import me.ivy.calc.CommandSignature;
import me.ivy.calc.CommandExecutionException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

@CommandName("SQRT")
public class SqrtCommand extends Command {
    @CommandSignature(count = 0, types = {}, requiredStackSize = 1)
    public void execute(ExecutionContext context) throws CommandExecutionException {
        double value = context.stack().pop();
        if (value < 0) {
            context.stack().push(value);
            throw new CommandExecutionException("SQRT of negative value");
        }
        context.stack().push(Math.sqrt(value));
    }
}
