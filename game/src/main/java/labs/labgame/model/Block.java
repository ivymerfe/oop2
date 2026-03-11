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

    public Block(GameModel model) {
        super(model);
    }

    @Override
    protected Body createBody() {
        Body body = BodyHelper.createBody(model.getWorld(), BodyDef.BodyType.DynamicBody, 0, 0, false, false);
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

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeFloat(health);
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        super.deserialize(in);
        health = in.readFloat();
    }
}
