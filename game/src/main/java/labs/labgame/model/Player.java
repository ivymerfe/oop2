package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Player extends Entity {
    private static final float WIDTH = 1.0f;
    private static final float HEIGHT = 1.5f;
    private static final float MAX_SPEED = 15.0f;
    private static final float MOVE_FORCE = 150.0f;
    private static final float STOP_FORCE = 50.0f;
    private static final float WALL_FORCE = 150.0f;
    private static final float JUMP_IMPULSE = 9.0f;
    private static final float MAX_HEALTH = 100.0f;
    private static final float REGEN = 10.0f;
    private static final float HEAD_DAMAGE = 80.0f;

    private float lookDirection = 1.0f;
    private float movement = 0.0f;
    private boolean jumping = false;
    public float health = MAX_HEALTH;
    private int headContacts = 0;
    private int sideContacts = 0;
    private int footContacts = 0;
    public int score = 0;
    public int maxScore = 0;

    public Player(GameModel model) {
        super(model);
        persistent = true;
    }

    @Override
    protected Body createBody() {
        Body body = BodyHelper.createBody(model.getWorld(), BodyDef.BodyType.DynamicBody, 0, 2, true, false);
        BodyHelper.createBox(body, WIDTH, HEIGHT, 1.0f, 0.5f, 0.0f, false, null, null);
        BodyHelper.createBox(body, WIDTH / 1.2f, 0.12f, 0.0f, 0.0f, 0.0f, true,
                new Vector2(0.0f, HEIGHT / 2.0f), Sensors.PlayerHead);
        BodyHelper.createBox(body, 0.1f, HEIGHT / 1.25f, 0.0f, 0.0f, 0.0f, true,
                new Vector2(-WIDTH / 2.0f, 0.0f), Sensors.PlayerSide);
        BodyHelper.createBox(body, 0.1f, HEIGHT / 1.25f, 0.0f, 0.0f, 0.0f, true,
                new Vector2(WIDTH / 2.0f, 0.0f), Sensors.PlayerSide);
        BodyHelper.createBox(body, WIDTH / 1.25f, 0.1f, 0.0f, 0.0f, 0.0f, true,
                new Vector2(0.0f, -HEIGHT / 2.0f), Sensors.PlayerFoot);
        return body;
    }

    public void update(float delta) {
        health = Math.min(MAX_HEALTH, health + REGEN*delta);
        maxScore = Math.max(score, maxScore);
        if (headContacts > 0) {
            health -= HEAD_DAMAGE*delta;
        }
        if (health <= 0 || body.getPosition().y < 0) {
            health = MAX_HEALTH;
            body.setTransform(0, 2, 0);
            score = 0;
            model.resetEntities();
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
            if (sideContacts > 0) {
                forceY += WALL_FORCE;
            }
        }
        getBody().applyForceToCenter(forceX, forceY, true);
    }

    public void heal(int amount) {
        health = Math.min(MAX_HEALTH, health + amount);
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

    public void shootAt(Vector2 target) {
        Vector2 playerPos = getPosition();
        Vector2 dir = target.cpy().sub(playerPos).nor();
        Vector2 pos = playerPos.cpy().add(dir.cpy().scl(2));
        if (pos.y < 0) pos.y = 0;

        model.addBullet(pos.x, pos.y, dir, getBody().getLinearVelocity(), this);
    }

    @Override
    public void onCollisionEnter(Entity other, Object data) {
        if (Sensors.PlayerHead.equals(data)) {
            headContacts += 1;
        }
        if (Sensors.PlayerSide.equals(data)) {
            sideContacts += 1;
        }
        if (Sensors.PlayerFoot.equals(data)) {
            footContacts += 1;
        }
    }

    @Override
    public void onCollisionExit(Entity other, Object data) {
        if (Sensors.PlayerHead.equals(data)) {
            headContacts = Math.max(0, headContacts - 1);
        }
        if (Sensors.PlayerSide.equals(data)) {
            sideContacts = Math.max(0, sideContacts - 1);
        }
        if (Sensors.PlayerFoot.equals(data)) {
            footContacts = Math.max(0, footContacts - 1);
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

    @Override
    public void remove() {
        return;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeFloat(health);
        out.writeInt(score);
        out.writeInt(maxScore);
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        super.deserialize(in);
        health = in.readFloat();
        score = in.readInt();
        maxScore = in.readInt();
    }
}
