package labs.labgame.gui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Box2D;
import labs.labgame.controller.InputController;
import labs.labgame.controller.GameController;
import labs.labgame.model.GameModel;

public class Main extends ApplicationAdapter {
    private GameModel model;
    private OrthographicCamera camera;

    private InputController inputController;
    private GameController gameController;
    private GameRenderer renderer;

    @Override
    public void create() {
        Box2D.init();
        model = new GameModel();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 32, 18);

        renderer = new GameRenderer(model, camera);
        inputController = new InputController(model, camera);
        gameController = new GameController(model, camera);
    }

    @Override
    public void render() {
        float delta = 1f/60;
        inputController.update(delta);
        gameController.update(delta);
        renderer.render();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        model.dispose();
    }

    public static void main(String[] args) {
        new Lwjgl3Application(new Main(), new Lwjgl3ApplicationConfiguration());
    }
}
