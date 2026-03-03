package me.ivy.game.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    public float width = 1;
    public float height = 1.5f;
    public float max_speed = 7;

    public float lookDirection = 1;
    public float movement = 0;
    public boolean jumping = false;

    public int contacts = 0;
    public int footContacts = 0;

    public final World world;
    public Body body;

    private float health = 1;

    public Player(World world, float x, float y) {
        this.world = world;
        createBody(x, y);
    }

    public void update(float delta) {
        Vector2 vel = body.getLinearVelocity();
        float forceX = 0;
        float forceY = 0;
        if (Math.abs(vel.x) < max_speed) {
            forceX = 80*movement;
            if (Math.signum(movement) != Math.signum(vel.x)) {
                forceX *= 4;
            }
            if (movement == 0) {
                forceX = -50 * Math.signum(vel.x);
            }
        }
        if (jumping) {
            if (footContacts > 0) {
                body.applyLinearImpulse(new Vector2(0, 9f), body.getWorldCenter(), true);
            }
            if (contacts > 0) forceY += 100;
        }
        body.applyForceToCenter(forceX, forceY, true);
    }

    private void createBody(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x, y);
        bdef.fixedRotation = true;
        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;

        fdef.friction = 0.5f;
        fdef.density = 1.0f;
        fdef.restitution = 0.0f;

        Fixture fix = body.createFixture(fdef);
        fix.setUserData(Registry.Player);

        shape.dispose();

        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(width / 2.5f, 0.05f, new Vector2(0, -width / 2 - 0.2f), 0);

        FixtureDef sdef = new FixtureDef();
        sdef.shape = sensorShape;
        sdef.isSensor = true;

        Fixture footSensorFixture = body.createFixture(sdef);
        footSensorFixture.setUserData(Registry.PlayerFoot);
        sensorShape.dispose();

        this.body = body;
    }
}
