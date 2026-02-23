package me.ivy.binary_commands;

import me.ivy.calc.Command;
import me.ivy.calc.CommandExecutionException;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

import java.util.List;

@CommandName({"+", "-", "*", "/"})
public class BinaryCommand extends Command {
    private final String operator;

    public BinaryCommand(String operator) {
        this.operator = operator;
    }

    @Override
    public String execute(ExecutionContext context, List<Object> args) throws CommandExecutionException {
        if (context.stack().size() < 2) {
            throw new CommandExecutionException("Not enough values on stack for " + operator);
        }
        double right = context.stack().pop();
        double left = context.stack().pop();

        if (operator.equals("/") && right == 0.0) {
            context.stack().push(left);
            context.stack().push(right);
            throw new CommandExecutionException("Division by zero");
        }

        double result = switch (operator) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> left / right;
            default -> throw new CommandExecutionException("Unknown operator: " + operator);
        };

        context.stack().push(result);
        return "";
    }
}
