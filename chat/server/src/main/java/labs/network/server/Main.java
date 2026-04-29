package labs.network.server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Main {
    private static final int DEFAULT_PORT = 6666;

    private static final Path SAVE_PATH = Path.of("chat.bin");

    public static void main(String[] args) {
        Properties properties = loadProperties();
        int port = parseIntProperty(properties, "port", DEFAULT_PORT);
        boolean loggingEnabled = Boolean.parseBoolean(properties.getProperty("loggingEnabled", "true"));

        Server server = new Server(SAVE_PATH, port, loggingEnabled);
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        Path path = Path.of("server.properties");
        if (Files.exists(path)) {
            try {
                properties.load(Files.newBufferedReader(path, StandardCharsets.UTF_8));
                return properties;
            } catch (IOException ignored) {
            }
        }
        try (var stream = Server.class.getClassLoader().getResourceAsStream("server.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ignored) {
        }
        return properties;
    }

    private static int parseIntProperty(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
