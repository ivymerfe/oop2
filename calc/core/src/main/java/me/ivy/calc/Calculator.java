package me.ivy.calc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main calculator facade for text program execution and programmatic command control.
 */
public class Calculator {
    private static final Logger logger = LogManager.getLogger(Calculator.class);

    private final Stack stack;
    private final Variables variables;
    private final ExecutionContext context;
    private final CommandFactory factory;

    /**
     * Creates calculator with empty stack and variables.
     */
    public Calculator() {
        this.stack = new Stack();
        this.variables = new Variables();
        this.context = new ExecutionContext(stack, variables.asMap());
        this.factory = new CommandFactory();
        logger.info("Calculator initialized");
    }

    /**
     * Executes command program text.
     * Supports comments after '#' and command separators ';'.
     *
     * @param program multiline program text
     * @return collected output lines or error lines
     */
    public String execute(String program) {
        StringBuilder output = new StringBuilder();
        if (program == null) {
            logger.warn("Program input is null");
            return "";
        }
        int expressionCount = 0;
        for (String line : program.split("\n")) {
            String trimmed = stripComment(line).trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            for (String chunk : trimmed.split(";")) {
                String expr = chunk.trim();
                if (expr.isEmpty()) {
                    continue;
                }
                expressionCount++;
                String result = executeExpression(expr);
                if (!result.isEmpty()) {
                    if (output.length() > 0) {
                        output.append("\n");
                    }
                    output.append(result);
                }
            }
        }
        logger.info("Executed {} expressions", expressionCount);
        return output.toString();
    }

    /**
     * Removes trailing comment from a line.
     *
     * @param line source line
     * @return line without comment part
     */
    private String stripComment(String line) {
        int idx = line.indexOf('#');
        if (idx < 0) {
            return line;
        }
        return line.substring(0, idx);
    }

    /**
     * Executes one command expression string.
     *
     * @param expr expression like "PUSH 10"
     * @return command output or error line
     */
    private String executeExpression(String expr) {
        String[] tokens = expr.trim().split("\\s+");
        if (tokens.length == 0) {
            return "";
        }
        String commandName = tokens[0];
        List<Object> args = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
            args.add(tokens[i]);
        }

        return executeCommand(commandName, args);
    }

    /**
     * Registers custom command instance under command name.
     *
     * @param commandName command token
     * @param command command instance
     */
    public void registerCommand(String commandName, Command command) {
        factory.registerCommand(commandName, command);
        logger.info("Custom command registered: {}", commandName);
    }

    /**
     * Executes command by name with vararg arguments.
     *
     * @param commandName command token
     * @param args command arguments
     * @return command output or error line
     */
    public String executeCommand(String commandName, Object... args) {
        return executeCommand(commandName, new ArrayList<>(Arrays.asList(args)));
    }

    /**
     * Executes command by name with argument list.
     *
     * @param commandName command token
     * @param args command arguments
     * @return command output or error line
     */
    public String executeCommand(String commandName, List<Object> args) {
        try {
            Command cmd = factory.getCommand(commandName);
            String result = cmd.execute(context, args);
            return result;
        } catch (CommandException e) {
            logger.warn("Command failed: {}. Error: {}", commandName, e.getMessage());
            return "ERR " + e.getMessage();
        }
    }

    /**
     * Returns stack values as strings for UI.
     *
     * @return stack items
     */
    public List<String> getStackItems() {
        return stack.getDisplayItems();
    }

    /**
     * Returns variable items as strings for UI.
     *
     * @return variable items
     */
    public List<String> getVariableItems() {
        return variables.getDisplayItems();
    }

    /**
     * Returns underlying stack object.
     *
     * @return stack
     */
    public Stack getStack() {
        return stack;
    }

    /**
     * Returns underlying variables object.
     *
     * @return variables
     */
    public Variables getVariables() {
        return variables;
    }
}
