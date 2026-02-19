package me.ivy.calc.commands;

import me.ivy.calc.Command;
import me.ivy.calc.Stack;
import me.ivy.calc.Variables;

public class SqrtCommand extends Command {
    public SqrtCommand(Stack stack, Variables variables) {
        super(stack, variables);
    }

    @Override
    public void execute(String[] args) throws CommandException {
        if (stack.isEmpty()) {
            throw new CommandException("SQRT on empty stack");
        }
        double value = stack.pop();
        if (value < 0) {
            stack.push(value);
            throw new CommandException("SQRT of negative value");
        }
        stack.push(Math.sqrt(value));
    }
}
