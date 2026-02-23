package me.ivy.basic_commands;

import me.ivy.calc.Command;
import me.ivy.calc.CommandExecutionException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

import java.util.List;

@CommandName("POP")
public class PopCommand extends Command {
    public PopCommand(ExecutionContext context) {
        super(context);
    }

    @Override
    public String execute(List<Object> args) throws CommandExecutionException {
        if (stack().isEmpty()) {
            throw new CommandExecutionException("POP on empty stack");
        }
        stack().pop();
        return "";
    }
}
