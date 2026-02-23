package me.ivy.calc;

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

public class CommandFactory {
    private static final String CONFIG_RESOURCE = "command-jars.conf";
    private final Map<String, Class<? extends Command>> commands = new HashMap<>();

    public CommandFactory() {
        loadConfiguredCommands();
    }

    public Command getCommand(String commandName) throws CommandException {
        String cmd = commandName.toUpperCase(Locale.ROOT);
        Class<? extends Command> commandClass = commands.get(cmd);
        if (commandClass == null) {
            throw new UnknownCommandException("Unknown command: " + commandName);
        }
        return instantiate(commandClass, cmd);
    }

    private void loadConfiguredCommands() {
        List<String> jars = loadJarPathsFromConfig();
        for (String jarPath : jars) {
            List<Class<?>> discovered = loadCommandClassesFromJar(jarPath);
            for (Class<?> type : discovered) {
                registerAnnotatedCommand(type);
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
            throw new IllegalStateException("Cannot read command config: " + CONFIG_RESOURCE, e);
        }
        return jars;
    }

    private List<Class<?>> loadCommandClassesFromJar(String jarPathLine) {
        Path jarPath = Path.of(jarPathLine);
        if (!jarPath.isAbsolute()) {
            jarPath = Path.of(System.getProperty("user.dir")).resolve(jarPathLine);
        }
        if (!Files.exists(jarPath)) {
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
            throw new IllegalStateException("Cannot load commands from jar: " + jarPath, e);
        }
        return classes;
    }

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
            commands.put(name.toUpperCase(Locale.ROOT), commandClass);
        }
    }

    private Command instantiate(Class<? extends Command> commandClass, String commandName) throws CommandException {
        try {
            Constructor<? extends Command> ctorWithName = commandClass.getConstructor(String.class);
            return ctorWithName.newInstance(commandName);
        } catch (NoSuchMethodException ignored) {
            try {
                Constructor<? extends Command> ctor = commandClass.getConstructor();
                return ctor.newInstance();
            } catch (NoSuchMethodException e) {
                throw new CommandException("Command constructor not found: " + commandClass.getSimpleName(), e);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new CommandException("Cannot instantiate command: " + commandClass.getSimpleName(), e);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new CommandException("Cannot instantiate command: " + commandClass.getSimpleName(), e);
        }
    }
}
