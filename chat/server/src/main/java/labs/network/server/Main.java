package labs.network.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Main {
    private static final int DEFAULT_PORT = 5000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 120000;

    public static void main(String[] args) {
        Properties properties = loadProperties();
        int port = parseIntProperty(properties, "port", DEFAULT_PORT);
        int readTimeout = parseIntProperty(properties, "readTimeoutMillis", DEFAULT_READ_TIMEOUT_MS);
        boolean loggingEnabled = Boolean.parseBoolean(properties.getProperty("loggingEnabled", "true"));

        Server server = new Server(port, readTimeout, loggingEnabled);
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
