package me.ivy.basic_commands;

import me.ivy.calc.Command;
import me.ivy.calc.CommandExecutionException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

import java.util.List;

@CommandName("POP")
public class PopCommand extends Command {
    @Override
    public String execute(ExecutionContext context, List<Object> args) throws CommandExecutionException {
        if (context.stack().isEmpty()) {
            throw new CommandExecutionException("POP on empty stack");
        }
        context.stack().pop();
        return "";
    }
}
