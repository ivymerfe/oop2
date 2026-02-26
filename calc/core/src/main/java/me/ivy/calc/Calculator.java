package me.ivy.calc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Calculator {
    private static final Logger logger = LogManager.getLogger(Calculator.class);

    private final Stack stack;
    private final Variables variables;
    private final ExecutionContext context;
    private final CommandFactory factory;
    private final CommandParser parser;
    private final Map<String, Command> initializedCommands;

    public Calculator(CommandFactory factory) {
        this.stack = new Stack();
        this.variables = new Variables();
        this.context = new ExecutionContext(stack, variables.asMap());
        this.factory = factory;
        this.parser = new CommandParser();
        this.initializedCommands = new HashMap<>();
        initializeRegisteredCommands();
        logger.info("Calculator initialized with {} command instances", initializedCommands.size());
    }

    public void execute(String program) throws CommandException {
        List<CommandParser.ParsedCommand> parsed = parser.parseProgram(program, factory);
        for (CommandParser.ParsedCommand parsedCommand : parsed) {
            executeParsedCommand(parsedCommand);
        }
    }

    public void registerCommand(String commandName, Class<? extends Command> commandClass) {
        factory.registerCommand(commandName, commandClass);
        initializedCommands.put(normalizeCommandName(commandName), instantiateCommand(commandClass));
    }

    public void executeCommand(String commandName, String... args) throws CommandException {
        String expression = commandName;
        for (String arg : args) {
            expression += " " + arg;
        }

        try {
            CommandParser.ParsedCommand parsed = parser.parseExpression(expression, factory);
            executeParsedCommand(parsed);
        } catch (CommandException e) {
            logger.warn("Command execution failed: {}. Error: {}", commandName, e.getMessage());
            throw e;
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

    private void initializeRegisteredCommands() {
        for (Map.Entry<String, Class<? extends Command>> entry : factory.getRegisteredCommandClasses().entrySet()) {
            initializedCommands.put(entry.getKey(), instantiateCommand(entry.getValue()));
        }
    }

    private Command instantiateCommand(Class<? extends Command> commandClass) {
        try {
            return commandClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate command: " + commandClass.getSimpleName(), e);
        }
    }

    private void executeParsedCommand(CommandParser.ParsedCommand parsed) throws CommandException {
        String commandName = parsed.commandName();
        Command command = initializedCommands.get(commandName);
        if (command == null) {
            throw new UnknownCommandException("Unknown command: " + commandName);
        }
        if (stack.size() < parsed.requiredStackSize()) {
            throw new CommandArgumentsException("Not enough values on stack for " + commandName);
        }

        try {
            Object[] invokeArgs = new Object[parsed.typedArguments().length + 1];
            invokeArgs[0] = context;
            System.arraycopy(parsed.typedArguments(), 0, invokeArgs, 1, parsed.typedArguments().length);
            parsed.executeMethod().invoke(command, invokeArgs);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access execute method for command " + commandName, e);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof CommandException commandException) {
                throw commandException;
            }
            throw new IllegalStateException("Unexpected command failure for " + commandName, target);
        }
    }

    private String normalizeCommandName(String commandName) {
        return commandName.trim().toUpperCase(Locale.ROOT);
    }
}
