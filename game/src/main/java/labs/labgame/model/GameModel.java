package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class GameModel {
    private final World world;
    private final Player player;
    private final Ground ground;
    private final Blocks blocks;
    private float time = 0.0f;

    public GameModel() {
        this.world = new World(new Vector2(0.0f, -40.0f), true);

        this.player = new Player(world, 0.0f, 10.0f);
        this.ground = new Ground(world);
        this.blocks = new Blocks(world);
    }

    public void update(float delta) {
        time += delta;
        player.update(delta);
        world.step(delta, 6, 2);
    }

    public void dispose() {
        world.dispose();
    }

    public float getTime() {
        return time;
    }

    public World getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }

    public Ground getGround() {
        return ground;
    }

    public Blocks getBlocks() {
        return blocks;
    }
}
