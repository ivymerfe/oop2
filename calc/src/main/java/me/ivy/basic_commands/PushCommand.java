package me.ivy.basic_commands;

import me.ivy.calc.Command;
import me.ivy.calc.CommandArgumentsException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

import java.util.List;
import java.util.OptionalDouble;

@CommandName("PUSH")
public class PushCommand extends Command {
    @Override
    public String execute(ExecutionContext context, List<Object> args) throws CommandArgumentsException {
        String token = requireStringArgument(args, 0, "value");
        OptionalDouble value = resolveNumericToken(context, token);
        if (value.isEmpty()) {
            throw new CommandArgumentsException("Unknown value: " + token);
        }
        context.stack().push(value.getAsDouble());
        return "";
    }
}
