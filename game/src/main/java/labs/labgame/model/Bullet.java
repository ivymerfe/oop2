package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

public class Bullet extends Entity {
    public static final float RADIUS = 0.5f;
    public static final float SPEED = 20.0f;
    public static final float EXPLOSION_RADIUS = 1.6f;
    public static final float EXPLOSION_POWER = 10.0f;
    public static final float EXPLOSION_DAMAGE = 25.0f;

    private final GameModel game;
    private final Entity owner;
    private float lifeTime = 0.0f;

    public Bullet(GameModel game, Entity owner, float x, float y, Vector2 direction, Vector2 initialVelocity) {
        this.game = game;
        Body body = BodyHelper.createBody(game.getWorld(), BodyDef.BodyType.DynamicBody, x, y, true, false);
        body.setGravityScale(0.5f);

        BodyHelper.createCircle(body, RADIUS, 0.1f, 0.0f, 0.0f, false, null, null);
        body.setLinearVelocity(initialVelocity.add(direction.x * SPEED, direction.y * SPEED));
        super(body);
        this.owner = owner;
    }

    @Override
    public void update(float delta) {
        lifeTime += delta;
    }

    @Override
    public void damage(float amount) {

    }

    @Override
    public void onCollisionEnter(Entity other, Object myFixtureData) {
        game.addExplosion(getBody().getPosition(), EXPLOSION_RADIUS, EXPLOSION_POWER, EXPLOSION_DAMAGE);
        remove();
    }

    @Override
    public void onCollisionExit(Entity other, Object myFixtureData) {

    }

    public float getLifeTime() {
        return lifeTime;
    }
}