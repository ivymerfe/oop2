package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    private static final float WIDTH = 1.0f;
    private static final float HEIGHT = 1.5f;
    private static final float MAX_SPEED = 15.0f;
    private static final float MOVE_FORCE = 150.0f;
    private static final float STOP_FORCE = 50.0f;
    private static final float JUMP_IMPULSE = 9.0f;
    private static final float WALL_PUSH_FORCE = 100.0f;

    private float lookDirection = 1.0f;
    private float movement = 0.0f;
    private boolean jumping = false;

    private int contacts = 0;
    private int footContacts = 0;

    private final Body body;

    public Player(World world, float x, float y) {
        this.body = createBody(world, x, y);
    }

    public void update(float delta) {
        Vector2 velocity = body.getLinearVelocity();
        float forceX;
        float forceY = 0.0f;

        float frac = velocity.x / MAX_SPEED; // [-1;1]
        float forceMove = MOVE_FORCE * movement * Math.max(0, 1 - frac*frac*frac*frac);
        float forceStop = STOP_FORCE * Math.signum(velocity.x);
        forceX = forceMove - forceStop;
        if (jumping) {
            if (footContacts > 0) {
                body.applyLinearImpulse(new Vector2(0.0f, JUMP_IMPULSE), body.getWorldCenter(), true);
            }
            if (contacts > 0) {
                forceY += WALL_PUSH_FORCE;
            }
        }
        body.applyForceToCenter(forceX, forceY, true);
    }

    private Body createBody(World world, float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x, y);
        bdef.fixedRotation = true;
        Body createdBody = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(WIDTH / 2.0f, HEIGHT / 2.0f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0.5f;
        fdef.density = 1.0f;
        fdef.restitution = 0.0f;

        Fixture bodyFixture = createdBody.createFixture(fdef);
        bodyFixture.setUserData(Registry.Player);
        shape.dispose();

        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(WIDTH / 2.5f, 0.05f, new Vector2(0.0f, -HEIGHT / 2.0f), 0.0f);

        FixtureDef sdef = new FixtureDef();
        sdef.shape = sensorShape;
        sdef.isSensor = true;
        Fixture footSensorFixture = createdBody.createFixture(sdef);
        footSensorFixture.setUserData(Registry.PlayerFoot);
        sensorShape.dispose();

        return createdBody;
    }

    public Body getBody() {
        return body;
    }

    public void setMovement(float movement) {
        this.movement = movement;
        if (movement != 0.0f) {
            this.lookDirection = movement;
        }
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public void incrementFootContacts() {
        footContacts += 1;
    }

    public void decrementFootContacts() {
        footContacts = Math.max(0, footContacts - 1);
    }

    public void incrementContacts() {
        contacts += 1;
    }

    public void decrementContacts() {
        contacts = Math.max(0, contacts - 1);
    }

    public float getX() {
        return body.getPosition().x;
    }

    public float getY() {
        return body.getPosition().y;
    }

    public float getWidth() {
        return WIDTH;
    }

    public float getHeight() {
        return HEIGHT;
    }

    public float getLookDirection() {
        return lookDirection;
    }
}
