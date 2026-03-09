package labs.labgame.tui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.Screen.RefreshType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import labs.labgame.controller.TuiInputController;
import labs.labgame.model.GameModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main extends ApplicationAdapter {
    public static final int FPS = 30;
    private static final Path SAVE_PATH = Path.of("save.bin");

    private Screen screen;
    private GameModel model;
    private TuiInputController inputController;
    private TuiRenderer renderer;

    @Override
    public void create() {
        Box2D.init();
        model = loadModel();
        inputController = new TuiInputController(model);
        renderer = new TuiRenderer(model);

        try {
            DefaultTerminalFactory factory = new DefaultTerminalFactory();
            factory.setForceAWTOverSwing(false);
            factory.setForceTextTerminal(true);
            factory.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG);
            screen = factory.createScreen();
            screen.startScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        screen.setCursorPosition(null);
    }

    @Override
    public void render() {
        if (inputController.isQuitRequested()) {
            Gdx.app.exit();
            return;
        }
        try {
            screen.doResizeIfNecessary();
            TuiRenderer.Viewport viewport = renderer.createViewport(screen.getTerminalSize());
            KeyStroke keyStroke;
            while ((keyStroke = screen.pollInput()) != null) {
                inputController.handleInput(keyStroke);
            }
            float delta = 1f/FPS;
            inputController.update(viewport);
            model.update(delta);

            renderer.render(screen);
            screen.refresh(RefreshType.DELTA);
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        saveModel();
        if (model != null) {
            model.dispose();
        }
        if (screen != null) {
            try {
                screen.stopScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private GameModel loadModel() {
        if (!Files.exists(SAVE_PATH)) {
            return new GameModel();
        }

        try (InputStream inputStream = Files.newInputStream(SAVE_PATH)) {
            return GameModel.deserialize(inputStream);
        } catch (IOException exception) {
            exception.printStackTrace();
            return new GameModel();
        }
    }

    private void saveModel() {
        if (model == null) {
            return;
        }

        try (OutputStream outputStream = Files.newOutputStream(SAVE_PATH)) {
            model.serialize(outputStream);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    static void main(String[] args) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = FPS;
        new HeadlessApplication(new Main(), config);
    }
}
