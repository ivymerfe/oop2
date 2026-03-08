package labs.labgame.model;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class Block extends Entity {
    public static final float SIZE = 1.2f;

    private float health = 200;

    public Block(World world, float x, float y) {
        super(createBlockBody(world, x, y));
    }

    private static Body createBlockBody(World world, float x, float y) {
        Body body = BodyHelper.createBody(world, BodyDef.BodyType.DynamicBody, x, y, false, false);
        BodyHelper.createBox(body, SIZE, SIZE, 3.0f, 0.8f, 0.0f, false, null, null);
        return body;
    }

    public float getSize() {
        return SIZE;
    }

    @Override
    public void update(float delta) {
        if (health <= 0) remove();
    }

    @Override
    public void damage(float amount) {
        health -= amount;
    }

    @Override
    public void onCollisionEnter(Entity other, Object myFixtureData) {

    }

    @Override
    public void onCollisionExit(Entity other, Object myFixtureData) {

    }
}