package labs.network.load_tester;

public class App {
    public static void main(String[] args) {
        int exitCode = new AppController().run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
