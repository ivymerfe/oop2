package me.ivy.binary_commands;

import me.ivy.calc.CommandSignature;
import me.ivy.calc.Command;
import me.ivy.calc.CommandExecutionException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

@CommandName("+")
public class AddCommand extends Command {
    @CommandSignature(count = 0, types = {}, requiredStackSize = 2)
    public void execute(ExecutionContext context) throws CommandExecutionException {
        double right = context.stack().pop();
        double left = context.stack().pop();
        context.stack().push(left + right);
    }
}
