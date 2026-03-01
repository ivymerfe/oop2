package me.ivy.game.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    public float width = 1;
    public float height = 1.5f;
    public float speed = 3;

    public float direction = 1;

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
        body.applyLinearImpulse(new Vector2(0, 30f), body.getWorldCenter(), true);
    }

    private void createBody(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x, y);
        bdef.fixedRotation = true;
        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width, height);
        body.createFixture(shape, 1.0f);
        shape.dispose();

        this.body = body;
    }
}
