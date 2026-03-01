package me.ivy.game.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.World;

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
        body.createFixture(shape, 0);
        shape.dispose();
    }
}
