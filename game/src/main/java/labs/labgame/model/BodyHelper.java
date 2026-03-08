package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class BodyHelper {
    public static Body createBody(World world, BodyDef.BodyType type, float x, float y,
                                  boolean fixedRotation, boolean bullet) {
        BodyDef definition = new BodyDef();
        definition.type = type;
        definition.position.set(x, y);
        definition.fixedRotation = fixedRotation;
        definition.bullet = bullet;
        return world.createBody(definition);
    }


    public static Fixture createCircle(Body body, float radius, float density,
                                       float friction, float restitution, boolean sensor,
                                       Vector2 center, Object userData) {
        CircleShape shape = new CircleShape();
        if (center != null) {
            shape.setPosition(center);
        }
        shape.setRadius(radius);

        FixtureDef definition = new FixtureDef();
        definition.shape = shape;
        definition.density = density;
        definition.friction = friction;
        definition.restitution = restitution;
        definition.isSensor = sensor;

        Fixture fixture = body.createFixture(definition);
        fixture.setUserData(userData);
        shape.dispose();
        return fixture;
    }

    public static Fixture createBox(Body body, float width, float height, float density,
                                    float friction, float restitution, boolean sensor,
                                    Vector2 center, Object userData) {
        PolygonShape shape = new PolygonShape();
        if (center == null) {
            shape.setAsBox(width / 2.0f, height / 2.0f);
        } else {
            shape.setAsBox(width / 2.0f, height / 2.0f, center, 0.0f);
        }

        FixtureDef definition = new FixtureDef();
        definition.shape = shape;
        definition.density = density;
        definition.friction = friction;
        definition.restitution = restitution;
        definition.isSensor = sensor;

        Fixture fixture = body.createFixture(definition);
        fixture.setUserData(userData);
        shape.dispose();
        return fixture;
    }
}
