package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Bullet extends Entity {
    public static final float RADIUS = 0.5f;
    public static final float SPEED = 25.0f;
    public static final float EXPLOSION_RADIUS = 1.6f;
    public static final float EXPLOSION_POWER = 5.0f;
    public static final float EXPLOSION_DAMAGE = 25.0f;

    private int ownerId;
    private float lifeTime = 0.0f;

    public Bullet(GameModel model) {
        super(model);
    }

    @Override
    protected Body createBody() {
        Body body = BodyHelper.createBody(model.getWorld(), BodyDef.BodyType.DynamicBody, 0, 0, true, false);
        body.setGravityScale(0.5f);
        BodyHelper.createCircle(body, RADIUS, 0.1f, 0.0f, 0.0f, false, null, null);
        return body;
    }

    public void setMovement(Vector2 direction, Vector2 initialVelocity) {
        body.setLinearVelocity(initialVelocity.add(direction.scl(SPEED)));
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
        boolean damagePlayer = ownerId != model.getPlayer().id;
        model.addExplosion(getBody().getPosition(), EXPLOSION_RADIUS, EXPLOSION_POWER, EXPLOSION_DAMAGE, damagePlayer);
        remove();
    }

    @Override
    public void onCollisionExit(Entity other, Object data) {

    }

    public void setOwner(int ownerId) {
        this.ownerId = ownerId;
    }

    public float getLifeTime() {
        return lifeTime;
    }

    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeInt(ownerId);
        out.writeFloat(lifeTime);
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        super.deserialize(in);
        ownerId = in.readInt();
        lifeTime = in.readFloat();
    }
}
