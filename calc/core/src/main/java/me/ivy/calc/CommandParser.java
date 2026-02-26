package me.ivy.calc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CommandParser {
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    public List<ParsedCommand> parseProgram(String program, CommandFactory factory)
            throws CommandException {
        List<ParsedCommand> commands = new ArrayList<>();
        if (program == null || program.isBlank()) {
            return commands;
        }

        for (String line : program.split("\\n")) {
            String trimmed = stripComment(line).trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            for (String chunk : trimmed.split(";")) {
                String expression = chunk.trim();
                if (expression.isEmpty()) {
                    continue;
                }
                ParsedCommand parsedCommand = parseExpression(expression, factory);
                commands.add(parsedCommand);
            }
        }
        return commands;
    }

    public ParsedCommand parseExpression(String expression, CommandFactory factory)
            throws CommandException {
        String[] tokens = expression.trim().split("\\s+");
        if (tokens.length == 0) {
            throw new CommandArgumentsException("Empty command expression");
        }

        String rawCommandName = tokens[0];
        String commandName = normalizeCommandName(rawCommandName);
        Class<? extends Command> commandClass = factory.getCommandClass(commandName);

        List<String> rawArgs = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
            rawArgs.add(tokens[i]);
        }

        Method method = resolveExecuteMethod(rawCommandName, commandClass, rawArgs);
        CommandSignature signature = method.getAnnotation(CommandSignature.class);
        Object[] typedArgs = convertArguments(rawArgs, signature.types());
        return new ParsedCommand(commandName, method, typedArgs, signature.requiredStackSize());
    }

    private Method resolveExecuteMethod(String commandName, Class<? extends Command> commandClass, List<String> rawArgs)
            throws CommandArgumentsException {
        List<Method> candidates = new ArrayList<>();
        for (Method method : commandClass.getMethods()) {
            if (!"execute".equals(method.getName())) {
                continue;
            }
            CommandSignature signature = method.getAnnotation(CommandSignature.class);
            if (signature == null) {
                continue;
            }
            if (signature.count() != signature.types().length) {
                throw new IllegalStateException("Invalid @CommandSignature on " + commandClass.getSimpleName());
            }
            if (signature.count() != rawArgs.size()) {
                continue;
            }
            if (isCompatible(rawArgs, signature.types())) {
                validateMethodSignature(commandClass, method, signature);
                candidates.add(method);
            }
        }

        if (candidates.isEmpty()) {
            throw new CommandArgumentsException("Invalid arguments for command " + commandName);
        }

        if (candidates.size() > 1) {
            throw new IllegalStateException("Ambiguous execute overloads for " + commandClass.getSimpleName());
        }

        return candidates.getFirst();
    }

    private void validateMethodSignature(Class<? extends Command> commandClass, Method method, CommandSignature signature) {
        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length != signature.count() + 1 || parameters[0] != ExecutionContext.class) {
            throw new IllegalStateException("Invalid execute signature in " + commandClass.getSimpleName());
        }
        for (int i = 0; i < signature.types().length; i++) {
            Class<?> expected = expectedClass(signature.types()[i]);
            if (!parameters[i + 1].equals(expected)) {
                throw new IllegalStateException(
                        "Invalid argument type in execute signature for " + commandClass.getSimpleName());
            }
        }
    }

    private boolean isCompatible(List<String> rawArgs, ArgumentType[] types) {
        for (int i = 0; i < rawArgs.size(); i++) {
            String raw = rawArgs.get(i);
            ArgumentType type = types[i];
            if (!isCompatible(raw, type)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCompatible(String raw, ArgumentType type) {
        return switch (type) {
            case NUMBER -> isNumber(raw);
            case IDENTIFIER -> isIdentifier(raw);
        };
    }

    private Object[] convertArguments(List<String> rawArgs, ArgumentType[] types) {
        Object[] converted = new Object[rawArgs.size()];
        for (int i = 0; i < rawArgs.size(); i++) {
            converted[i] = switch (types[i]) {
                case NUMBER -> Double.parseDouble(rawArgs.get(i));
                case IDENTIFIER -> rawArgs.get(i);
            };
        }
        return converted;
    }

    private Class<?> expectedClass(ArgumentType type) {
        return switch (type) {
            case NUMBER -> Double.class;
            case IDENTIFIER -> String.class;
        };
    }

    private boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isIdentifier(String token) {
        return IDENTIFIER_PATTERN.matcher(token).matches();
    }

    private String stripComment(String line) {
        int idx = line.indexOf('#');
        if (idx < 0) {
            return line;
        }
        return line.substring(0, idx);
    }

    private String normalizeCommandName(String commandName) {
        return commandName.trim().toUpperCase(Locale.ROOT);
    }

    public record ParsedCommand(
            String commandName,
            Method executeMethod,
            Object[] typedArguments,
            int requiredStackSize
    ) {
    }
}