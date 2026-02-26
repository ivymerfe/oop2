package me.ivy.binary_commands;

import me.ivy.calc.CommandSignature;
import me.ivy.calc.Command;
import me.ivy.calc.CommandExecutionException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

@CommandName("/")
public class DivideCommand extends Command {
    @CommandSignature(count = 0, types = {}, requiredStackSize = 2)
    public void execute(ExecutionContext context) throws CommandExecutionException {
        double right = context.stack().pop();
        double left = context.stack().pop();
        if (right == 0) {
            context.stack().push(left);
            context.stack().push(right);
            throw new CommandExecutionException("Division by zero");

        }
        context.stack().push(left / right);
    }
}
