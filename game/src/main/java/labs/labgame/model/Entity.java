package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Entity {
    private static int nextId = 0;

    protected final int id;
    protected final Body body;
    private boolean removed = false;
    public boolean persistent = false;

    protected Entity(Body body) {
        this.id = nextId++;
        this.body = body;
        this.body.setUserData(this);
    }

    public int getId() {
        return id;
    }

    public abstract void update(float delta);

    public abstract void damage(float amount);

    public abstract void onCollisionEnter(Entity other, Object myFixtureData);

    public abstract void onCollisionExit(Entity other, Object myFixtureData);

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

    public boolean isRemoved() {
        return removed;
    }

    public void remove() {
        removed = true;
    }
}
