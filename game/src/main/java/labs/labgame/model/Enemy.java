package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

public class Enemy extends Entity {
    public static final float WIDTH = 0.8f;
    public static final float HEIGHT = 1.8f;
    private static final float MAX_SPEED = 12.0f;
    private static final float MOVE_FORCE = 150.0f;
    private static final float STOP_FORCE = 25.0f;
    private static final float JUMP_IMPULSE = 12.5f;
    private static final float SHOOT_COOLDOWN = 0.5f;
    private static final float AGGRO_RANGE = 30.0f;
    private static final float PREFERRED_RANGE = 8.0f;
    private static final float MIN_RANGE = 4.5f;
    private static final float SHOOT_RANGE = 25.0f;
    private static final float SELF_DESTRUCT_RADIUS = 2.0f;
    private static final float SELF_DESTRUCT_POWER = 50.0f;
    private static final float SELF_DESTRUCT_DAMAGE = 40.0f;
    private static final float SELF_DESTRUCT_TIME = 1.0f;

    private final GameModel model;
    private float lookDirection = -1.0f;
    private float health = 100.0f;
    private float shootCooldown = 0.6f;
    private int footContacts = 0;
    private int blockContacts = 0;
    private float lastContactTime = 0;
    private int headBlockContacts = 0;
    private boolean selfDestructTriggered = false;

    public Enemy(GameModel model, float x, float y) {
        this.model = model;
        Body body = BodyHelper.createBody(model.getWorld(), BodyDef.BodyType.DynamicBody, x, y, true, false);
        BodyHelper.createBox(body, WIDTH, HEIGHT, 1.2f, 0.4f, 0.0f, false, null, null);
        BodyHelper.createBox(body, WIDTH / 1.2f, 0.12f, 0.0f, 0.0f, 0.0f, true,
                new Vector2(0.0f, -HEIGHT / 2.0f), Sensors.EnemyFoot);
        BodyHelper.createBox(body, WIDTH / 1.2f, 0.12f, 0.0f, 0.0f, 0.0f, true,
                new Vector2(0.0f, HEIGHT / 2.0f), Sensors.EnemyHead);
        super(body);
    }

    public void update(float delta) {
        if (isRemoved()) {
            return;
        }
        if (health <= 0) {
            model.getPlayer().score += 1;
            remove();
            return;
        }

        shootCooldown = Math.max(0.0f, shootCooldown - delta);

        if (headBlockContacts > 0 || selfDestructTriggered) {
            detonate();
            return;
        }
        if (blockContacts == 0) {
            lastContactTime = model.getTime();
        }
        if (model.getTime() - lastContactTime > SELF_DESTRUCT_TIME) {
            detonate();
            return;
        }

        if (footContacts > 0) {
            getBody().applyLinearImpulse(new Vector2(0.0f, JUMP_IMPULSE), getBody().getWorldCenter(), true);
        }

        Player player = model.getPlayer();
        Vector2 velocity = getBody().getLinearVelocity();
        float distanceX = player.getX() - getX();
        float absDistanceX = Math.abs(distanceX);
        float directionToPlayer = distanceX == 0.0f ? lookDirection : Math.signum(distanceX);

        if (directionToPlayer != 0.0f) {
            lookDirection = directionToPlayer;
        }

        boolean lineOfSight = hasLineOfSight(player);
        float desiredRange = PREFERRED_RANGE + 1.5f * (float) Math.sin(model.getTime() * 0.8f + id * 0.65f);
        float movement;

        if (absDistanceX > AGGRO_RANGE) {
            movement = directionToPlayer;
        } else if (absDistanceX < MIN_RANGE) {
            movement = -directionToPlayer;
        } else {
            float targetX = player.getX() - directionToPlayer * (lineOfSight ? desiredRange : MIN_RANGE);
            movement = clamp((targetX - getX()) / 2.5f, -1.0f, 1.0f);
        }

        float speedRatio = velocity.x / MAX_SPEED;
        float forceMove = MOVE_FORCE * movement * Math.max(0.0f, 1.0f - speedRatio * speedRatio * speedRatio * speedRatio);
        float forceStop = STOP_FORCE * Math.signum(velocity.x);
        getBody().applyForceToCenter(forceMove - forceStop, 0.0f, true);

        if (lineOfSight && absDistanceX <= SHOOT_RANGE) {
            shootAt(player);
        }
    }

    private void shootAt(Player player) {
        if (shootCooldown > 0.0f) {
            return;
        }

        Vector2 spawn = getPosition().cpy().add(lookDirection * (WIDTH + Bullet.RADIUS), HEIGHT * 0.05f);
        Vector2 inheritedVelocity = getBody().getLinearVelocity().cpy().scl(0.35f);
        Vector2 direction = computeAimDirection(player, spawn, inheritedVelocity);

        model.addBullet(this, spawn, spawn.cpy().add(direction), inheritedVelocity);
        shootCooldown = SHOOT_COOLDOWN + 0.35f * (float) Math.abs(Math.sin(model.getTime() + id));
    }

    private Vector2 computeAimDirection(Player player, Vector2 spawn, Vector2 inheritedVelocity) {
        Vector2 fallback = player.getPosition().cpy().add(0.0f, player.getHeight() * 0.2f).sub(spawn);
        if (fallback.isZero(0.001f)) {
            fallback.set(lookDirection, 0.0f);
        }

        Vector2 targetBase = player.getPosition().cpy().add(0.0f, player.getHeight() * 0.2f);
        Vector2 targetVelocity = player.getBody().getLinearVelocity().cpy();
        Vector2 gravity = model.getWorld().getGravity().cpy().scl(0.5f);
        Vector2 bestDirection = fallback.cpy().nor();
        float bestError = Float.MAX_VALUE;

        for (float time = 0.15f; time <= 2.2f; time += 0.05f) {
            Vector2 displacement = targetBase.cpy().mulAdd(targetVelocity, time).sub(spawn)
                    .sub(inheritedVelocity.x * time, inheritedVelocity.y * time)
                    .sub(0.5f * gravity.x * time * time, 0.5f * gravity.y * time * time);
            Vector2 shotVelocity = displacement.scl(1.0f / time);
            float error = Math.abs(shotVelocity.len() - Bullet.SPEED);
            if (error < bestError && !shotVelocity.isZero(0.001f)) {
                bestError = error;
                bestDirection = shotVelocity.nor();
            }
        }

        return bestDirection;
    }

    private boolean hasLineOfSight(Player player) {
        final boolean[] blocked = {false};
        Vector2 from = getPosition().cpy().add(0.0f, HEIGHT * 0.1f);
        Vector2 to = player.getPosition().cpy().add(0.0f, player.getHeight() * 0.2f);

        model.getWorld().rayCast((fixture, point, normal, fraction) -> {
            Body hitBody = fixture.getBody();
            if (hitBody == getBody() || hitBody == player.getBody()) {
                return -1.0f;
            }
            Object hit = hitBody.getUserData();
            if (hit instanceof Bullet) {
                return -1.0f;
            }
            blocked[0] = true;
            return fraction;
        }, from, to);

        return !blocked[0];
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void detonate() {
        if (isRemoved()) {
            return;
        }
        model.getPlayer().score += 1;
        model.addExplosion(getBody().getPosition().cpy(), SELF_DESTRUCT_RADIUS, SELF_DESTRUCT_POWER, SELF_DESTRUCT_DAMAGE);
        remove();
    }

    @Override
    public void onCollisionEnter(Entity other, Object myFixtureData) {
        if (Sensors.EnemyFoot.equals(myFixtureData)) {
            footContacts += 1;
            return;
        }
        if (other instanceof Block) {
            blockContacts += 1;
            if (Sensors.EnemyHead.equals(myFixtureData)) {
                headBlockContacts += 1;
                selfDestructTriggered = true;
            }
        }
    }

    @Override
    public void onCollisionExit(Entity other, Object myFixtureData) {
        if (Sensors.EnemyFoot.equals(myFixtureData)) {
            footContacts = Math.max(0, footContacts - 1);
        }
        if (other instanceof Block) {
            blockContacts = Math.max(0, blockContacts - 1);
            if (Sensors.EnemyHead.equals(myFixtureData)) {
                headBlockContacts = Math.max(0, headBlockContacts - 1);
            }
        }
    }

    public void damage(float amount) {
        health = Math.max(0.0f, health - amount);
    }

    public float getLookDirection() {
        return lookDirection;
    }

    public float getWidth() {
        return WIDTH;
    }

    public float getHeight() {
        return HEIGHT;
    }
}