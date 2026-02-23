package me.ivy.basic_commands;

import me.ivy.calc.Command;
import me.ivy.calc.CommandExecutionException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

import java.util.List;

@CommandName("SQRT")
public class SqrtCommand extends Command {
    @Override
    public String execute(ExecutionContext context, List<Object> args) throws CommandExecutionException {
        if (context.stack().isEmpty()) {
            throw new CommandExecutionException("SQRT on empty stack");
        }
        double value = context.stack().pop();
        if (value < 0) {
            context.stack().push(value);
            throw new CommandExecutionException("SQRT of negative value");
        }
        context.stack().push(Math.sqrt(value));
        return "";
    }
}
