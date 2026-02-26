package me.ivy.basic_commands;

import me.ivy.calc.Command;
import me.ivy.calc.CommandSignature;
import me.ivy.calc.CommandName;
import me.ivy.calc.ExecutionContext;

@CommandName("POP")
public class PopCommand extends Command {
    @CommandSignature(count = 0, types = {}, requiredStackSize = 1)
    public void execute(ExecutionContext context) {
        context.stack().pop();
    }
}
