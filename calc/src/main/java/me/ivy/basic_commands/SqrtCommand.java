package me.ivy.basic_commands;

import me.ivy.calc.Command;
import me.ivy.calc.CommandExecutionException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

import java.util.List;

@CommandName("SQRT")
public class SqrtCommand extends Command {
    public SqrtCommand(ExecutionContext context) {
        super(context);
    }

    @Override
    public String execute(List<Object> args) throws CommandExecutionException {
        if (stack().isEmpty()) {
            throw new CommandExecutionException("SQRT on empty stack");
        }
        double value = stack().pop();
        if (value < 0) {
            stack().push(value);
            throw new CommandExecutionException("SQRT of negative value");
        }
        stack().push(Math.sqrt(value));
        return "";
    }
}
