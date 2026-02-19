package me.ivy.calc.commands;

import me.ivy.calc.Command;
import me.ivy.calc.Stack;
import me.ivy.calc.Variables;

public class PrintCommand extends Command {
    public PrintCommand(Stack stack, Variables variables) {
        super(stack, variables);
    }

    @Override
    public void execute(String[] args) throws CommandException {
        if (stack.isEmpty()) {
            throw new CommandException("PRINT on empty stack");
        }
        // Output is handled by ConsoleOutput
    }

    public String getOutput() throws CommandException {
        if (stack.isEmpty()) {
            throw new CommandException("PRINT on empty stack");
        }
        return String.valueOf(stack.peek());
    }
}
