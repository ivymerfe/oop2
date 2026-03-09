package labs.labgame.gui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Box2D;
import labs.labgame.controller.GameController;
import labs.labgame.controller.InputController;
import labs.labgame.model.GameModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main extends ApplicationAdapter {
    private static final Path SAVE_PATH = Path.of("save.bin");

    private GameModel model;
    private OrthographicCamera camera;

    private InputController inputController;
    private GameController gameController;
    private GameRenderer renderer;
    private SoundManager soundManager;

    @Override
    public void create() {
        Box2D.init();
        model = loadModel();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 32, 18);

        inputController = new InputController(model, camera);
        gameController = new GameController(model, camera);
        renderer = new GameRenderer(model, camera);
        soundManager = new SoundManager(model, camera);
    }

    @Override
    public void render() {
        float delta = 1f/60;
        inputController.update(delta);
        gameController.update(delta);
        renderer.render();
        soundManager.update();
    }

    @Override
    public void dispose() {
        saveModel();
        renderer.dispose();
        model.dispose();
    }

    public static void main(String[] args) {
        new Lwjgl3Application(new Main(), new Lwjgl3ApplicationConfiguration());
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
        try (OutputStream outputStream = Files.newOutputStream(SAVE_PATH)) {
            model.serialize(outputStream);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
