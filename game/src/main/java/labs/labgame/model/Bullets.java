package labs.labgame.model;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.HashMap;
import java.util.Map;

public class Bullets {
    public static final float SIZE = 1.0f;

    private final World world;

    private final HashMap<Integer, Body> bullets = new HashMap<>();
    private int nextBulletId = 0;

    public Bullets(World world) {
        this.world = world;
    }

    public int create(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = false;
        bdef.position.set(x, y);
        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(SIZE / 2.0f, SIZE / 2.0f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0.8f;
        fdef.density = 3.0f;
        fdef.restitution = 0.0f;

        Fixture fix = body.createFixture(fdef);
        fix.setUserData(Registry.Block);
        shape.dispose();

        int id = nextBulletId;
        nextBulletId += 1;
        bullets.put(id, body);
        return id;
    }

    public void remove(int id) {
        Body body = bullets.remove(id);
        if (body != null) {
            world.destroyBody(body);
        }
    }

    public Map<Integer, Body> getBullets() {
        return bullets;
    }

    public World getWorld() {
        return world;
    }

    public int getNextBulletId() {
        return nextBulletId;
    }

    public void setNextBulletId(int nextBulletId) {
        this.nextBulletId = nextBulletId;
    }

    public void clear() {
        for (Body body : bullets.values()) {
            world.destroyBody(body);
        }
        bullets.clear();
    }
}
