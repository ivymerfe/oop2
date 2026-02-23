package me.ivy.calc.commands;

import me.ivy.calc.Command;
import me.ivy.calc.Stack;
import me.ivy.calc.Variables;

public class BinaryCommand extends Command {
    private final String operator;

    public BinaryCommand(Stack stack, Variables variables, String operator) {
        super(stack, variables);
        this.operator = operator;
    }

    @Override
    public void execute(String[] args) throws CommandException {
        if (stack.size() < 2) {
            throw new CommandException("Not enough values on stack for " + operator);
        }
        double right = stack.pop();
        double left = stack.pop();

        if (operator.equals("/") && right == 0.0) {
            stack.push(left);
            stack.push(right);
            throw new CommandException("Division by zero");
        }

        double result = switch (operator) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> left / right;
            default -> throw new CommandException("Unknown operator: " + operator);
        };

        stack.push(result);
    }
}
