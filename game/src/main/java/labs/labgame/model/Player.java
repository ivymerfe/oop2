package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class Player extends Entity {
    private static final float WIDTH = 1.0f;
    private static final float HEIGHT = 1.5f;
    private static final float MAX_SPEED = 15.0f;
    private static final float MOVE_FORCE = 150.0f;
    private static final float STOP_FORCE = 50.0f;
    private static final float JUMP_IMPULSE = 9.0f;
    private static final float MAX_HEALTH = 100.0f;
    private static final float REGEN = 10.0f;
    private static final float HEAD_DAMAGE = 80.0f;

    private float lookDirection = 1.0f;
    private float movement = 0.0f;
    private boolean jumping = false;
    private float health = MAX_HEALTH;
    private int footContacts = 0;
    private int headContacts = 0;
    public int score = 0;

    private GameModel game;

    public Player(GameModel game) {
        this.game = game;

        Body body = BodyHelper.createBody(game.getWorld(), BodyDef.BodyType.DynamicBody, 0, 2, true, false);
        BodyHelper.createBox(body, WIDTH, HEIGHT, 1.0f, 0.5f, 0.0f, false, null, null);
        BodyHelper.createBox(body, WIDTH / 1.25f, 0.1f, 0.0f, 0.0f, 0.0f, true,
                new Vector2(0.0f, -HEIGHT / 2.0f), Sensors.PlayerFoot);
        BodyHelper.createBox(body, WIDTH / 1.2f, 0.12f, 0.0f, 0.0f, 0.0f, true,
                new Vector2(0.0f, HEIGHT / 2.0f), Sensors.PlayerHead);

        super(body);
    }

    public void update(float delta) {
        health = Math.min(MAX_HEALTH, health + REGEN*delta);
        if (headContacts > 0) {
            health -= HEAD_DAMAGE*delta;
        }
        if (health <= 0) {
            health = MAX_HEALTH;
            body.setTransform(0, 2, 0);
            score = 0;
            game.resetEntities();
            return;
        }
        Vector2 velocity = getBody().getLinearVelocity();
        float forceX;
        float forceY = 0.0f;

        float frac = velocity.x / MAX_SPEED; // [-1;1]
        float forceMove = MOVE_FORCE * movement * Math.max(0.0f, 1.0f - frac * frac * frac * frac);
        float forceStop = STOP_FORCE * Math.signum(velocity.x);
        forceX = forceMove - forceStop;
        if (jumping) {
            if (footContacts > 0) {
                getBody().applyLinearImpulse(new Vector2(0.0f, JUMP_IMPULSE), getBody().getWorldCenter(), true);
            }
        }
        getBody().applyForceToCenter(forceX, forceY, true);
    }

    @Override
    public void damage(float amount) {
        health -= amount;
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

    @Override
    public void onCollisionEnter(Entity other, Object myFixtureData) {
        if (Sensors.PlayerFoot.equals(myFixtureData)) {
            footContacts += 1;
        }
        if (Sensors.PlayerHead.equals(myFixtureData)) {
            headContacts += 1;
        }
    }

    @Override
    public void onCollisionExit(Entity other, Object myFixtureData) {
        if (Sensors.PlayerFoot.equals(myFixtureData)) {
            footContacts = Math.max(0, footContacts - 1);
        }
        if (Sensors.PlayerHead.equals(myFixtureData)) {
            headContacts = Math.max(0, headContacts - 1);
        }
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

    public float getHealth() {
        return health;
    }
}
