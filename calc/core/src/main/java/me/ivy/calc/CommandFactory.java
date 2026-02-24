package me.ivy.calc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

/**
 * Factory and registry for calculator commands.
 * Loads annotated command implementations from configured jar files and supports runtime registration.
 */
public class CommandFactory {
    private static final Logger logger = LogManager.getLogger(CommandFactory.class);

    private static final String CONFIG_RESOURCE = "command-jars.conf";
    private final Map<String, Command> commands = new HashMap<>();

    /**
     * Creates factory and loads configured commands.
     */
    public CommandFactory() {
        logger.info("Command factory initialization started");
        loadConfiguredCommands();
        logger.info("Command factory loaded {} commands", commands.size());
    }

    /**
     * Returns command instance by name.
     *
     * @param commandName command token
     * @return command instance
     * @throws CommandException when command is unknown
     */
    public Command getCommand(String commandName) throws CommandException {
        String cmd = normalizeCommandName(commandName);
        Command command = commands.get(cmd);
        if (command == null) {
            logger.warn("Unknown command requested: {}", commandName);
            throw new UnknownCommandException("Unknown command: " + commandName);
        }
        logger.debug("Command resolved: {}", cmd);
        return command;
    }

    /**
     * Registers custom command instance under command name.
     *
     * @param commandName command token
     * @param command command instance
     */
    public void registerCommand(String commandName, Command command) {
        String normalized = normalizeCommandName(commandName);
        commands.put(normalized, command);
        logger.info("Command registered: {}", normalized);
    }

    /**
     * Loads commands from jars listed in factory config resource.
     */
    private void loadConfiguredCommands() {
        List<String> jars = loadJarPathsFromConfig();
        logger.info("Loading commands from {} configured jar entries", jars.size());
        for (String jarPath : jars) {
            List<Class<?>> discovered = loadCommandClassesFromJar(jarPath);
            logger.info("Discovered {} command classes in {}", discovered.size(), jarPath);
            for (Class<?> type : discovered) {
                registerAnnotatedCommand(type);
            }
        }
    }

    /**
     * Reads configured jar paths from resource file.
     *
     * @return jar path entries
     */
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

    /**
     * Scans one jar file and returns command classes with {@link CommandName} annotation.
     *
     * @param jarPathLine configured jar path
     * @return discovered command classes
     */
    private List<Class<?>> loadCommandClassesFromJar(String jarPathLine) {
        Path jarPath = Path.of(jarPathLine);
        if (!jarPath.isAbsolute()) {
            jarPath = Path.of(System.getProperty("user.dir")).resolve(jarPathLine);
        }
        if (!Files.exists(jarPath)) {
            logger.warn("Command jar not found: {}", jarPath);
            return List.of();
        }

        List<Class<?>> classes = new ArrayList<>();
        try {
            URL jarUrl = jarPath.toUri().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl },
                    CommandFactory.class.getClassLoader());
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
                        classes.add(clazz);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Cannot load commands from jar: {}", jarPath, e);
            throw new IllegalStateException("Cannot load commands from jar: " + jarPath, e);
        }
        return classes;
    }

    /**
     * Registers command class for all names from its annotation.
     *
     * @param rawClass class to inspect
     */
    private void registerAnnotatedCommand(Class<?> rawClass) {
        if (!Command.class.isAssignableFrom(rawClass)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Class<? extends Command> commandClass = (Class<? extends Command>) rawClass;
        CommandName annotation = commandClass.getAnnotation(CommandName.class);
        if (annotation == null) {
            return;
        }

        for (String name : annotation.value()) {
            String commandName = normalizeCommandName(name);
            commands.put(commandName, createCommand(commandClass, commandName));
            logger.info("Command registered: {} -> {}", commandName, commandClass.getSimpleName());
        }
    }

    /**
     * Normalizes command token for stable key lookup.
     *
     * @param commandName raw command token
     * @return normalized token
     */
    private String normalizeCommandName(String commandName) {
        return commandName.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Creates command instance using constructor with command name or default constructor.
     *
     * @param commandClass command implementation class
     * @param commandName normalized command name
     * @return created command instance
     */
    private Command createCommand(Class<? extends Command> commandClass, String commandName) {
        try {
            Constructor<? extends Command> ctorWithName = commandClass.getConstructor(String.class);
            return ctorWithName.newInstance(commandName);
        } catch (NoSuchMethodException ignored) {
            try {
                Constructor<? extends Command> ctor = commandClass.getConstructor();
                return ctor.newInstance();
            } catch (NoSuchMethodException e) {
                logger.error("Command constructor missing for {}", commandClass.getSimpleName(), e);
                throw new IllegalStateException("Command constructor not found: " + commandClass.getSimpleName(), e);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                logger.error("Cannot instantiate command {}", commandClass.getSimpleName(), e);
                throw new IllegalStateException("Cannot instantiate command: " + commandClass.getSimpleName(), e);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Cannot instantiate command {}", commandClass.getSimpleName(), e);
            throw new IllegalStateException("Cannot instantiate command: " + commandClass.getSimpleName(), e);
        }
    }
}
