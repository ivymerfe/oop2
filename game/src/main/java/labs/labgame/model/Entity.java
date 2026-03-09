package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Entity {
    private static int nextId = 0;

    protected final int id;
    protected final Body body;
    private boolean removed = false;
    public boolean persistent = false;

    protected Entity(Body body) {
        this(body, getNextId());
    }

    protected Entity(Body body, int id) {
        this.id = id;
        nextId = Math.max(nextId, id + 1);
        this.body = body;
        this.body.setUserData(this);
    }

    protected static int getNextId() {
        return nextId++;
    }

    public int getId() {
        return id;
    }

    public abstract void update(float delta);

    public abstract void damage(float amount);

    public abstract void onCollisionEnter(Entity other, Object data);

    public abstract void onCollisionExit(Entity other, Object data);

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

    public float getExplosionResistance() {
        return 1.0f;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void remove() {
        removed = true;
    }

    protected void serializeEntity(DataOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeBoolean(removed);
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getAngle());
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeFloat(body.getAngularVelocity());
    }

    protected static EntityState deserializeEntity(DataInputStream in) throws IOException {
        int id = in.readInt();
        boolean removed = in.readBoolean();
        float x = in.readFloat();
        float y = in.readFloat();
        float angle = in.readFloat();
        float velocityX = in.readFloat();
        float velocityY = in.readFloat();
        float angularVelocity = in.readFloat();
        return new EntityState(id, removed, x, y, angle, velocityX, velocityY, angularVelocity);
    }

    protected void applyState(EntityState state) {
        body.setTransform(state.x(), state.y(), state.angle());
        body.setLinearVelocity(state.velocityX(), state.velocityY());
        body.setAngularVelocity(state.angularVelocity());
    }

    protected record EntityState(
            int id,
            boolean removed,
            float x,
            float y,
            float angle,
            float velocityX,
            float velocityY,
            float angularVelocity
    ) {
    }
}
