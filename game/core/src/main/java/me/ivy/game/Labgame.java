package me.ivy.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import me.ivy.game.controller.InputController;
import me.ivy.game.controller.WorldController;
import me.ivy.game.model.Blocks;
import me.ivy.game.model.Ground;
import me.ivy.game.model.Player;
import me.ivy.game.view.GameRenderer;

public class Labgame extends ApplicationAdapter {
    private World world;
    private OrthographicCamera camera;
    private Player player;
    private Ground ground;
    private Blocks blocks;

    private float time = 0;

    private InputController input;
    private WorldController worldController;
    private GameRenderer renderer;

    @Override
    public void create() {
        Box2D.init();

        world = new World(new Vector2(0, -40f), true);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 32, 18);

        player = new Player(world, 0, 10);
        ground = new Ground(world);
        blocks = new Blocks(world);

        renderer = new GameRenderer(this);
        input = new InputController(this);
        worldController = new WorldController(this);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        time += delta;

        input.update(delta);
        worldController.update(delta);
        player.update(delta);
        renderer.render();
    }

    @Override
    public void dispose() {
        world.dispose();
    }

    public World getWorld() {
        return world;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public float getTime() {
        return time;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Ground getGround() {
        return this.ground;
    }

    public Blocks getBlocks() {
        return this.blocks;
    }
}
