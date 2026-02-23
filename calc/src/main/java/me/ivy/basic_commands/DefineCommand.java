package me.ivy.basic_commands;

import me.ivy.calc.Command;
import me.ivy.calc.CommandArgumentsException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

import java.util.List;
import java.util.OptionalDouble;

@CommandName("DEFINE")
public class DefineCommand extends Command {
    public DefineCommand(ExecutionContext context) {
        super(context);
    }

    @Override
    public String execute(List<Object> args) throws CommandArgumentsException {
        String name = requireStringArgument(args, 0, "name");
        String token = requireStringArgument(args, 1, "value");
        OptionalDouble value = resolveNumericToken(token);
        if (value.isEmpty()) {
            throw new CommandArgumentsException("Unknown value: " + token);
        }
        variables().put(name, value.getAsDouble());
        return "";
    }
}
