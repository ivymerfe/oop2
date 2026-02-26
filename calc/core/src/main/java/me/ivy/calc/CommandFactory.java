package me.ivy.calc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CommandFactory {
    private static final Logger logger = LogManager.getLogger(CommandFactory.class);

    private static final String CONFIG_RESOURCE = "command-jars.conf";
    private final Map<String, Class<? extends Command>> commandClasses = new HashMap<>();

    public CommandFactory() {
        logger.info("Command factory initialization started");
        loadConfiguredCommands();
        logger.info("Command factory loaded {} command classes", commandClasses.size());
    }

    public Class<? extends Command> getCommandClass(String commandName) throws CommandException {
        String normalized = normalizeCommandName(commandName);
        Class<? extends Command> commandClass = commandClasses.get(normalized);
        if (commandClass == null) {
            logger.warn("Unknown command requested: {}", commandName);
            throw new UnknownCommandException("Unknown command: " + commandName);
        }
        return commandClass;
    }

    public Map<String, Class<? extends Command>> getRegisteredCommandClasses() {
        return Map.copyOf(commandClasses);
    }

    public void registerCommand(String commandName, Class<? extends Command> commandClass) {
        String normalized = normalizeCommandName(commandName);
        if (commandClasses.containsKey(normalized) && !commandClasses.get(normalized).equals(commandClass)) {
            throw new IllegalStateException("Command already registered: " + normalized);
        }
        commandClasses.put(normalized, commandClass);
        logger.info("Command class registered: {} -> {}", normalized, commandClass.getSimpleName());
    }

    public void registerAnnotatedCommand(Class<? extends Command> commandClass) {
        CommandName commandName = commandClass.getAnnotation(CommandName.class);
        if (commandName == null) {
            throw new IllegalArgumentException("Missing @CommandName for class " + commandClass.getName());
        }
        registerCommand(commandName.value(), commandClass);
    }

    private void loadConfiguredCommands() {
        List<String> jars = loadJarPathsFromConfig();
        logger.info("Loading command classes from {} configured jar entries", jars.size());
        for (String jarPath : jars) {
            List<Class<? extends Command>> discovered = loadCommandClassesFromJar(jarPath);
            logger.info("Discovered {} command classes in {}", discovered.size(), jarPath);
            for (Class<? extends Command> commandClass : discovered) {
                registerAnnotatedCommand(commandClass);
            }
        }
    }

    private List<String> loadJarPathsFromConfig() {
        List<String> jars = new ArrayList<>();
        try (InputStream stream = CommandFactory.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (stream == null) {
                return jars;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    jars.add(trimmed);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read command config: {}", CONFIG_RESOURCE, e);
            throw new IllegalStateException("Cannot read command config: " + CONFIG_RESOURCE, e);
        }
        return jars;
    }

    @SuppressWarnings("unchecked")
    private List<Class<? extends Command>> loadCommandClassesFromJar(String jarPathLine) {
        Path jarPath = Path.of(jarPathLine);
        if (!jarPath.isAbsolute()) {
            jarPath = Path.of(System.getProperty("user.dir")).resolve(jarPathLine);
        }
        if (!Files.exists(jarPath)) {
            logger.warn("Command jar not found: {}", jarPath);
            return List.of();
        }

        List<Class<? extends Command>> classes = new ArrayList<>();
        try {
            URL jarUrl = jarPath.toUri().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, CommandFactory.class.getClassLoader());
            try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.getName().endsWith(".class") || entry.isDirectory()) {
                        continue;
                    }
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    if (Command.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(CommandName.class)) {
                        classes.add((Class<? extends Command>) clazz);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Cannot load commands from jar: {}", jarPath, e);
            throw new IllegalStateException("Cannot load command classes from jar: " + jarPath, e);
        }
        return classes;
    }

    private String normalizeCommandName(String commandName) {
        return commandName.trim().toUpperCase(Locale.ROOT);
    }
}
