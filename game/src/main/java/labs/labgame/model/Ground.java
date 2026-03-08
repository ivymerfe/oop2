package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Ground extends Entity {
    public Ground(World world) {
        super(createGroundBody(world));
    }

    private static Body createGroundBody(World world) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(0, 0);
        Body staticBody = world.createBody(bdef);

        EdgeShape shape = new EdgeShape();
        shape.set(new Vector2(-100, 0), new Vector2(1000, 0));

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0.2f;
        fdef.density = 0.0f;
        fdef.restitution = 0.0f;

        staticBody.createFixture(fdef);
        shape.dispose();
        return staticBody;
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void damage(float amount) {

    }

    @Override
    public void onCollisionEnter(Entity other, Object myFixtureData) {

    }

    @Override
    public void onCollisionExit(Entity other, Object myFixtureData) {

    }
}
