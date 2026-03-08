package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;

public class ExplosionEffect extends Effect {
    private final float radius;

    public ExplosionEffect(Vector2 center, float radius) {
        super(center, 0.4f);
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }
}
