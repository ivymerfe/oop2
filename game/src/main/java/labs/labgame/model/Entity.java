package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Entity {
    protected final GameModel model;
    protected int id = -1;
    protected final Body body;
    private boolean removed = false;
    public boolean persistent = false;

    public Entity(GameModel model) {
        this.model = model;
        this.body = createBody();
        this.body.setUserData(this);
    }

    protected abstract Body createBody();

    public abstract void update(float delta);

    public abstract void damage(float amount);

    public abstract void onCollisionEnter(Entity other, Object data);

    public abstract void onCollisionExit(Entity other, Object data);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Body getBody() {
        return body;
    }

    public float getX() {
        return body.getPosition().x;
    }

    public float getY() {
        return body.getPosition().y;
    }

    public float getAngle() {
        return body.getAngle();
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void setPosition(float x, float y) {
        body.setTransform(x, y, body.getAngle());
    }

    public float getExplosionResistance() {
        return 1.0f;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void remove() {
        removed = true;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getAngle());
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeFloat(body.getAngularVelocity());
    }

    public void deserialize(DataInputStream in) throws IOException {
        int id = in.readInt();
        float x = in.readFloat();
        float y = in.readFloat();
        float angle = in.readFloat();
        float velocityX = in.readFloat();
        float velocityY = in.readFloat();
        float angularVelocity = in.readFloat();

        setId(id);
        body.setTransform(x, y, angle);
        body.setLinearVelocity(velocityX, velocityY);
        body.setAngularVelocity(angularVelocity);
    }
}
