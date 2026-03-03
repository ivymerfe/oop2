package me.ivy.game.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import me.ivy.game.Labgame;

import java.util.HashMap;

public class Blocks {
    public static final float SIZE = 1.2f;

    final World world;

    final HashMap<Integer, Body> blocks = new HashMap<>();
    int nextBlockId = 0;

    public Blocks(World world) {
        this.world = world;
    }

    public int create(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = false;
        bdef.position.set(x, y);
        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(SIZE / 2, SIZE / 2);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;

        fdef.friction = 0.8f;
        fdef.density = 3.0f;
        fdef.restitution = 0.0f;

        Fixture fix = body.createFixture(fdef);
        fix.setUserData(Registry.Block);

        shape.dispose();

        int id = this.nextBlockId;
        this.nextBlockId += 1;
        blocks.put(id, body);
        return id;
    }

    public void remove(int id) {
        if (this.blocks.containsKey(id)) {
            Body body = this.blocks.remove(id);
            world.destroyBody(body);
        }
    }

    public HashMap<Integer, Body> getBlocks() {
        return blocks;
    }
}
