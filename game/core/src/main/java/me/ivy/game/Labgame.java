package me.ivy.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import me.ivy.game.controller.InputController;
import me.ivy.game.model.Ground;
import me.ivy.game.model.Player;
import me.ivy.game.view.GameRenderer;

public class Labgame extends ApplicationAdapter {
    private World world;
    private Player player;
    private Ground ground;

    private InputController controller;
    private GameRenderer renderer;

    @Override
    public void create() {
        Box2D.init();

        world = new World(new Vector2(0, -20f), true);
        player = new Player(world, 0, 10);
        ground = new Ground(world);

        controller = new InputController(this);
        renderer = new GameRenderer(this);
    }

    @Override
    public void render() {
        controller.update(Gdx.graphics.getDeltaTime());
        renderer.render();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        world.dispose();
    }

    public World getWorld() {
        return world;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Ground getGround() {
        return this.ground;
    }
}
