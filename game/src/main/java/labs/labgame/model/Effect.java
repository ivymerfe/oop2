package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;

public class Effect {
    private final Vector2 position;
    private final float duration;
    private float time = 0.0f;

    public Effect(Vector2 position, float duration) {
        this.position = position;
        this.duration = duration;
    }

    public boolean update(float delta) {
        time += delta;
        return time >= duration;
    }

    public Vector2 getPosition() {
        return this.position;
    }

    public float getProgress() {
        return Math.min(1.0f, time / duration);
    }
}
