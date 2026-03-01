package me.ivy.game.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Ground {
    public final World world;
    public Body body;

    public Ground(World world) {
        this.world = world;
        createBody();
    }

    private void createBody() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(0, 0);
        Body body = world.createBody(bdef);

        EdgeShape shape = new EdgeShape();
        shape.set(new Vector2(-100, 0), new Vector2(100, 0));

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;

        fdef.friction = 0.7f;
        fdef.density = 0.0f;
        fdef.restitution = 0;

        Fixture fix = body.createFixture(fdef);
        fix.setUserData(Registry.Ground);

        shape.dispose();
    }
}
