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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CommandFactory {
    private static final String CONFIG_RESOURCE = "command-jars.conf";
    private final Map<String, CommandCreator> creators = new ConcurrentHashMap<>();

    public CommandFactory() {
        loadConfiguredCommands();
    }

    public Command createCommand(String commandName, ExecutionContext context) throws CommandException {
        String cmd = commandName.toUpperCase(Locale.ROOT);
        CommandCreator creator = creators.get(cmd);
        if (creator == null) {
            throw new UnknownCommandException("Unknown command: " + commandName);
        }
        return creator.create(context, cmd);
    }

    private void loadConfiguredCommands() {
        List<String> jars = loadJarPathsFromConfig();
        Set<Class<?>> discovered = new HashSet<>();
        for (String jarPath : jars) {
            discovered.addAll(loadCommandClassesFromJar(jarPath));
        }

        for (Class<?> type : discovered) {
            registerAnnotatedCommand(type);
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

        CommandCreator creator = buildCreator(commandClass);
        for (String name : annotation.value()) {
            creators.put(name.toUpperCase(Locale.ROOT), creator);
        }
    }

    private CommandCreator buildCreator(Class<? extends Command> commandClass) {
        try {
            Constructor<? extends Command> ctorWithName = commandClass.getConstructor(ExecutionContext.class,
                    String.class);
            return (context, commandName) -> instantiate(ctorWithName, context, commandName);
        } catch (NoSuchMethodException ignored) {
            try {
                Constructor<? extends Command> ctor = commandClass.getConstructor(ExecutionContext.class);
                return (context, commandName) -> instantiate(ctor, context);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Command constructor not found for " + commandClass.getName(), e);
            }
        }
    }

    private Command instantiate(Constructor<? extends Command> ctor, Object... args) throws CommandException {
        try {
            return ctor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new CommandException("Cannot instantiate command: " + ctor.getDeclaringClass().getSimpleName(), e);
        }
    }

    @FunctionalInterface
    private interface CommandCreator {
        Command create(ExecutionContext context, String commandName) throws CommandException;
    }
}
