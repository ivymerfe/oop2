package me.ivy.calc.commands;

import me.ivy.calc.Command;
import me.ivy.calc.Stack;
import me.ivy.calc.Variables;

public class PopCommand extends Command {
    public PopCommand(Stack stack, Variables variables) {
        super(stack, variables);
    }

    @Override
    public void execute(String[] args) throws CommandException {
        if (stack.isEmpty()) {
            throw new CommandException("POP on empty stack");
        }
        stack.pop();
    }
}
