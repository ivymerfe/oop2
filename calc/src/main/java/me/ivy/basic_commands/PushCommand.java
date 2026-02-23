package me.ivy.basic_commands;

import me.ivy.calc.Command;
import me.ivy.calc.CommandArgumentsException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

import java.util.List;
import java.util.OptionalDouble;

@CommandName("PUSH")
public class PushCommand extends Command {
    public PushCommand(ExecutionContext context) {
        super(context);
    }

    @Override
    public String execute(List<Object> args) throws CommandArgumentsException {
        String token = requireStringArgument(args, 0, "value");
        OptionalDouble value = resolveNumericToken(token);
        if (value.isEmpty()) {
            throw new CommandArgumentsException("Unknown value: " + token);
        }
        stack().push(value.getAsDouble());
        return "";
    }
}
