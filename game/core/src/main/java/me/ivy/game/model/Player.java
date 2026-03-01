package me.ivy.game.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    public float width = 1;
    public float height = 1.5f;
    public float speed = 6;

    public float direction = 1;
    public int contacts = 0;

    public final World world;
    public Body body;
    private float health;

    public Player(World world, float x, float y) {
        this.world = world;
        createBody(x, y);
    }

    public void move(float direction) {
        this.direction = direction;
        body.setLinearVelocity(speed*direction, body.getLinearVelocity().y);
    }

    public void jump() {
        if (contacts > 0) {
            body.applyLinearImpulse(new Vector2(0, 60f), body.getWorldCenter(), true);
        }
    }

    private void createBody(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x, y);
        bdef.fixedRotation = true;
        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width, height);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;

        fdef.friction = 0.7f;
        fdef.density = 1.0f;
        fdef.restitution = 0;

        Fixture fix = body.createFixture(fdef);
        fix.setUserData(Registry.Player);

        shape.dispose();

        this.body = body;
    }
}
