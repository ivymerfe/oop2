package labs.labgame.gui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Box2D;
import labs.labgame.controller.InputController;
import labs.labgame.controller.WorldController;
import labs.labgame.model.GameModel;

public class Main extends ApplicationAdapter {
    private GameModel model;
    private OrthographicCamera camera;

    private InputController input;
    private WorldController worldController;
    private GameRenderer renderer;

    @Override
    public void create() {
        Box2D.init();
        model = new GameModel();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 32, 18);

        renderer = new GameRenderer(model, camera);
        input = new InputController(model, camera);
        worldController = new WorldController(model, camera);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        input.update(delta);
        worldController.update(delta);
        renderer.render();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        model.dispose();
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config =    new Lwjgl3ApplicationConfiguration();
        config.setTitle("labgame");
        config.setWindowedMode(800, 600);
        new Lwjgl3Application(new Main(), config);
    }
}
