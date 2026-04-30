package labs.network.load_tester;

import labs.network.protocol.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class AppController {
    public int run(String[] args) {
        Config config;
        try {
            config = Config.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("[error] " + e.getMessage());
            printUsage();
            return 2;
        }

        if (config.help) {
            printUsage();
            return 0;
        }

        List<Client> clients = new ArrayList<>(config.clients);
        Stats stats = new Stats();
        CountDownLatch connectedLatch = new CountDownLatch(config.clients);

        System.out.printf("Starting load test: host=%s port=%d clients=%d messagesPerClient=%d serializer=%s%n",
                config.host, config.port, config.clients, config.messagesPerClient, config.serializerMode);

        for (int i = 0; i < config.clients; i++) {
            String username = config.usernamePrefix + i;
            Client client = new Client(new LoadListener(username, connectedLatch, stats));
            client.start(config.host, config.port, username, config.password, config.clientType, config.serializerMode);
            clients.add(client);
            if (config.rampUpMillis > 0) {
                sleepQuietly(config.rampUpMillis);
            }
        }

        try {
            boolean allConnected = connectedLatch.await(config.connectTimeoutSeconds, TimeUnit.SECONDS);
            if (!allConnected) {
                System.err.printf("[warn] connect timeout reached: connected=%d/%d%n", stats.connected.sum(), config.clients);
            }

            runSendPhase(config, clients, stats);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[error] interrupted");
            return 1;
        } finally {
            clients.forEach(Client::stop);
        }

        printReport(config, stats);
        return 0;
    }

    private void runSendPhase(Config config, List<Client> clients, Stats stats) throws InterruptedException {
        long startNs = System.nanoTime();
        CountDownLatch sendLatch = new CountDownLatch(clients.size());

        for (Client client : clients) {
            Thread.ofVirtual().start(() -> {
                try {
                    for (int i = 0; i < config.messagesPerClient; i++) {
                        if (client.sendChatMessage(config.messageTemplate + i)) {
                            stats.sent.increment();
                        } else {
                            stats.sendFailed.increment();
                        }
                        if (config.thinkTimeMillis > 0) {
                            sleepQuietly(config.thinkTimeMillis);
                        }
                    }
                } finally {
                    sendLatch.countDown();
                }
            });
        }

        boolean finished = sendLatch.await(config.sendTimeoutSeconds, TimeUnit.SECONDS);
        stats.durationNs.set(System.nanoTime() - startNs);
        if (!finished) {
            System.err.println("[warn] send timeout reached");
        }
    }

    private void printReport(Config config, Stats stats) {
        long sent = stats.sent.sum();
        long failed = stats.sendFailed.sum();
        long errors = stats.errors.sum();
        long connected = stats.connected.sum();
        long disconnected = stats.disconnected.sum();
        long received = stats.receivedMessages.sum();

        double seconds = Math.max(0.001, stats.durationNs.get() / 1_000_000_000.0);
        double throughput = sent / seconds;

        System.out.println();
        System.out.println("=== Load Test Report ===");
        System.out.printf("Target: %s:%d%n", config.host, config.port);
        System.out.printf("Clients: %d (connected: %d, disconnected events: %d)%n", config.clients, connected, disconnected);
        System.out.printf("Messages requested: %d%n", (long) config.clients * config.messagesPerClient);
        System.out.printf("Messages sent: %d%n", sent);
        System.out.printf("Send failed: %d%n", failed);
        System.out.printf("Client errors: %d%n", errors);
        System.out.printf("Incoming chat events: %d%n", received);
        System.out.printf("Send phase duration: %.3f sec%n", seconds);
        System.out.printf(Locale.ROOT, "Send throughput: %.2f msg/sec%n", throughput);
    }

    private void printUsage() {
        System.out.println("Usage: load_tester [options]");
        System.out.println("  --host <host>                 default: localhost");
        System.out.println("  --port <port>                 default: 6666");
        System.out.println("  --clients <count>             default: 50");
        System.out.println("  --messages <count>            messages per client, default: 200");
        System.out.println("  --serializer <xml|object>     default: xml");
        System.out.println("  --password <password>         default: empty");
        System.out.println("  --client-type <type>          default: load-tester");
        System.out.println("  --username-prefix <prefix>    default: lt-");
        System.out.println("  --message-template <prefix>   default: ping-");
        System.out.println("  --ramp-up-ms <ms>             delay between client starts, default: 0");
        System.out.println("  --think-time-ms <ms>          delay between messages, default: 0");
        System.out.println("  --connect-timeout-sec <sec>   default: 20");
        System.out.println("  --send-timeout-sec <sec>      default: 120");
        System.out.println("  --help");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class LoadListener implements Client.Listener {
        private final String username;
        private final CountDownLatch connectedLatch;
        private final Stats stats;
        private final AtomicInteger connectSignals = new AtomicInteger();

        private LoadListener(String username, CountDownLatch connectedLatch, Stats stats) {
            this.username = username;
            this.connectedLatch = connectedLatch;
            this.stats = stats;
        }

        @Override
        public void onConnecting(int attempt) {
            if (attempt == 0) {
                stats.connecting.increment();
            }
        }

        @Override
        public void onConnected(String username) {
            stats.connected.increment();
            if (connectSignals.getAndIncrement() == 0) {
                connectedLatch.countDown();
            }
        }

        @Override
        public void onDisconnected(String reason) {
            stats.disconnected.increment();
        }

        @Override
        public void onStopped() {
            if (connectSignals.getAndIncrement() == 0) {
                connectedLatch.countDown();
            }
        }

        @Override
        public void onChatMessage(String from, String text) {
            stats.receivedMessages.increment();
        }

        @Override
        public void onError(String text) {
            stats.errors.increment();
            if (stats.firstErrors.size() < 5) {
                synchronized (stats.firstErrors) {
                    if (stats.firstErrors.size() < 5) {
                        stats.firstErrors.add(username + ": " + text);
                    }
                }
            }
        }

        @Override
        public void onUserJoined(String username, String clientType) {
        }

        @Override
        public void onUserLeft(String username) {
        }

        @Override
        public void onUsers(List<UserInfo> users) {
        }
    }

    private static final class Stats {
        private final LongAdder connecting = new LongAdder();
        private final LongAdder connected = new LongAdder();
        private final LongAdder disconnected = new LongAdder();
        private final LongAdder sent = new LongAdder();
        private final LongAdder sendFailed = new LongAdder();
        private final LongAdder errors = new LongAdder();
        private final LongAdder receivedMessages = new LongAdder();
        private final java.util.concurrent.atomic.AtomicLong durationNs = new java.util.concurrent.atomic.AtomicLong();
        private final List<String> firstErrors = new ArrayList<>();
    }

    private static final class Config {
        private String host = "localhost";
        private int port = 6666;
        private int clients = 200;
        private int messagesPerClient = 2000;
        private String password = "";
        private String clientType = "load-tester";
        private String usernamePrefix = "lt-";
        private String messageTemplate = "ping-";
        private int rampUpMillis = 0;
        private int thinkTimeMillis = 0;
        private int connectTimeoutSeconds = 20;
        private int sendTimeoutSeconds = 120;
        private boolean help = false;
        private Client.SerializerMode serializerMode = Client.SerializerMode.XML;

        private static Config parse(String[] args) {
            Config cfg = new Config();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--host" -> cfg.host = requireValue(arg, args, ++i);
                    case "--port" -> cfg.port = parseInt(arg, requireValue(arg, args, ++i), 1, 65535);
                    case "--clients" -> cfg.clients = parseInt(arg, requireValue(arg, args, ++i), 1, 100_000);
                    case "--messages" -> cfg.messagesPerClient = parseInt(arg, requireValue(arg, args, ++i), 1, 1_000_000);
                    case "--password" -> cfg.password = requireValue(arg, args, ++i);
                    case "--client-type" -> cfg.clientType = requireValue(arg, args, ++i);
                    case "--username-prefix" -> cfg.usernamePrefix = requireValue(arg, args, ++i);
                    case "--message-template" -> cfg.messageTemplate = requireValue(arg, args, ++i);
                    case "--ramp-up-ms" -> cfg.rampUpMillis = parseInt(arg, requireValue(arg, args, ++i), 0, 60_000);
                    case "--think-time-ms" -> cfg.thinkTimeMillis = parseInt(arg, requireValue(arg, args, ++i), 0, 60_000);
                    case "--connect-timeout-sec" -> cfg.connectTimeoutSeconds = parseInt(arg, requireValue(arg, args, ++i), 1, 3600);
                    case "--send-timeout-sec" -> cfg.sendTimeoutSeconds = parseInt(arg, requireValue(arg, args, ++i), 1, 24 * 3600);
                    case "--serializer" -> cfg.serializerMode = parseSerializer(requireValue(arg, args, ++i));
                    case "--help" -> cfg.help = true;
                    default -> throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }
            return cfg;
        }

        private static String requireValue(String name, String[] args, int index) {
            if (index >= args.length) {
                throw new IllegalArgumentException("Missing value for " + name);
            }
            return args[index];
        }

        private static int parseInt(String name, String value, int min, int max) {
            try {
                int parsed = Integer.parseInt(value);
                if (parsed < min || parsed > max) {
                    throw new IllegalArgumentException(name + " must be in [" + min + ", " + max + "]");
                }
                return parsed;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer for " + name + ": " + value);
            }
        }

        private static Client.SerializerMode parseSerializer(String raw) {
            if ("xml".equalsIgnoreCase(raw)) {
                return Client.SerializerMode.XML;
            }
            if ("object".equalsIgnoreCase(raw)) {
                return Client.SerializerMode.OBJECT;
            }
            throw new IllegalArgumentException("Unsupported serializer: " + raw + ". Use xml or object");
        }
    }
}
