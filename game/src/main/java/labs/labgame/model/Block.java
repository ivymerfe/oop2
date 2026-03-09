package labs.labgame.model;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Block extends Entity {
    public static final float SIZE = 1.2f;

    private float health = 50;

    public Block(World world, float x, float y) {
        super(createBlockBody(world, x, y));
    }

    private Block(World world, EntityState state) {
        super(createBlockBody(world, state.x(), state.y()), state.id());
        applyState(state);
    }

    private static Body createBlockBody(World world, float x, float y) {
        Body body = BodyHelper.createBody(world, BodyDef.BodyType.DynamicBody, x, y, false, false);
        BodyHelper.createBox(body, SIZE, SIZE, 3.0f, 0.8f, 0.0f, false, null, null);
        return body;
    }

    @Override
    public float getExplosionResistance() {
        return 0.1f;
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
    public void onCollisionEnter(Entity other, Object data) {

    }

    @Override
    public void onCollisionExit(Entity other, Object data) {

    }

    public void serialize(DataOutputStream out) throws IOException {
        serializeEntity(out);
        out.writeFloat(health);
    }

    public static Block deserialize(World world, DataInputStream in) throws IOException {
        EntityState state = deserializeEntity(in);
        Block block = new Block(world, state);
        block.health = in.readFloat();
        return block;
    }
}