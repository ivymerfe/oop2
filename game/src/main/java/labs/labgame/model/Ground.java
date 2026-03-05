package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Ground {
    private final Body body;

    public Ground(World world) {
        this.body = createBody(world);
    }

    private Body createBody(World world) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(0, 0);
        Body staticBody = world.createBody(bdef);

        EdgeShape shape = new EdgeShape();
        shape.set(new Vector2(-100, 0), new Vector2(100, 0));

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0.2f;
        fdef.density = 0.0f;
        fdef.restitution = 0.0f;

        Fixture fixture = staticBody.createFixture(fdef);
        fixture.setUserData(Registry.Ground);
        shape.dispose();
        return staticBody;
    }

    public Body getBody() {
        return body;
    }
}
