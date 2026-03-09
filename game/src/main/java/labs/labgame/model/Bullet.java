package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Bullet extends Entity {
    public static final float RADIUS = 0.5f;
    public static final float SPEED = 20.0f;
    public static final float EXPLOSION_RADIUS = 1.6f;
    public static final float EXPLOSION_POWER = 5.0f;
    public static final float EXPLOSION_DAMAGE = 25.0f;

    private final GameModel game;
    private int ownerId;
    private float lifeTime = 0.0f;

    public Bullet(GameModel game, int ownerId, float x, float y, Vector2 direction, Vector2 initialVelocity) {
        this.game = game;
        this.ownerId = ownerId;

        Body body = BodyHelper.createBody(game.getWorld(), BodyDef.BodyType.DynamicBody, x, y, true, false);
        body.setGravityScale(0.5f);
        BodyHelper.createCircle(body, RADIUS, 0.1f, 0.0f, 0.0f, false, null, null);
        body.setLinearVelocity(initialVelocity.add(direction.x * SPEED, direction.y * SPEED));
        super(body);
    }

    @Override
    public void update(float delta) {
        lifeTime += delta;
    }

    @Override
    public void damage(float amount) {

    }

    @Override
    public void onCollisionEnter(Entity other, Object data) {
        game.addExplosion(getBody().getPosition(), EXPLOSION_RADIUS, EXPLOSION_POWER, EXPLOSION_DAMAGE);
        remove();
    }

    @Override
    public void onCollisionExit(Entity other, Object data) {

    }

    public float getLifeTime() {
        return lifeTime;
    }

    public void serialize(DataOutputStream out) throws IOException {
        serializeEntity(out);
        out.writeInt(ownerId);
        out.writeFloat(lifeTime);
    }

    public static Bullet deserialize(GameModel game, DataInputStream in) throws IOException {
        EntityState state = deserializeEntity(in);
        int ownerId = in.readInt();
        Bullet bullet = new Bullet(game, ownerId, state.x(), state.y(), new Vector2(1.0f, 0.0f), new Vector2());
        bullet.lifeTime = in.readFloat();
        bullet.applyState(state);
        return bullet;
    }
}