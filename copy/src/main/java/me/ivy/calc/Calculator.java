package me.ivy.calc;

import me.ivy.calc.commands.PrintCommand;

import java.util.List;

public class Calculator {
    private final Stack stack;
    private final Variables variables;
    private final CommandFactory factory;

    public Calculator() {
        this.stack = new Stack();
        this.variables = new Variables();
        this.factory = new CommandFactory(stack, variables);
    }

    public String execute(String program) {
        StringBuilder output = new StringBuilder();
        if (program == null) {
            return "";
        }
        for (String line : program.split("\n")) {
            String trimmed = stripComment(line).trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            for (String chunk : trimmed.split(";")) {
                String command = chunk.trim();
                if (command.isEmpty()) {
                    continue;
                }
                String result = executeCommand(command);
                if (!result.isEmpty()) {
                    if (output.length() > 0) {
                        output.append("\n");
                    }
                    output.append(result);
                }
            }
        }
        return output.toString();
    }

    private String stripComment(String line) {
        int idx = line.indexOf('#');
        if (idx < 0) {
            return line;
        }
        return line.substring(0, idx);
    }

    private String executeCommand(String command) {
        String[] tokens = command.trim().split("\\s+");
        if (tokens.length == 0) {
            return "";
        }
        String commandName = tokens[0];
        
        try {
            Command cmd = factory.createCommand(commandName);
            
            // Special handling for PRINT
            if (cmd instanceof PrintCommand) {
                return ((PrintCommand) cmd).getOutput();
            } else {
                cmd.execute(tokens);
                return "";
            }
        } catch (Command.CommandException e) {
            return "ERR " + e.getMessage();
        }
    }

    public List<String> getStackItems() {
        return stack.getDisplayItems();
    }

    public List<String> getVariableItems() {
        return variables.getDisplayItems();
    }

    public Stack getStack() {
        return stack;
    }

    public Variables getVariables() {
        return variables;
    }
}
