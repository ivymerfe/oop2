package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Ground extends Entity {
    public Ground(GameModel model) {
        super(model);
        persistent = true;
    }

    @Override
    protected Body createBody() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(0, 0);
        Body staticBody = model.getWorld().createBody(bdef);

        EdgeShape shape = new EdgeShape();
        shape.set(new Vector2(-100, 0), new Vector2(10000, 0));

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
    public void onCollisionEnter(Entity other, Object data) {

    }

    @Override
    public void onCollisionExit(Entity other, Object data) {

    }
}
