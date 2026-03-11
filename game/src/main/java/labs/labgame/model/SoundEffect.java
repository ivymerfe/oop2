package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;

public class SoundEffect {
    public enum SoundType {
        Explosion
    }

    SoundType type;
    Vector2 position;
    float volume;

    public SoundEffect(SoundType type, float x, float y, float volume) {
        this.type = type;
        this.position = new Vector2(x, y);
        this.volume = volume;
    }

    public SoundType getType() {
        return type;
    }

    public Vector2 getPosition() {
        return position;
    }
}
