package labs.labgame.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import labs.labgame.model.GameModel;
import labs.labgame.model.SoundEffect;

public class SoundManager implements Disposable {
    Sound explosion;

    GameModel model;
    Camera camera;

    public SoundManager(GameModel model, Camera camera) {
        this.model = model;
        this.camera = camera;

        explosion = Gdx.audio.newSound(Gdx.files.classpath("explosion.mp3"));
    }

    public void update() {
        Vector2 targetPos = model.getPlayer().getPosition();

        for (SoundEffect eff : model.getSounds()) {
            if (eff.getType() == SoundEffect.SoundType.Explosion) {
                playPositional(explosion, targetPos, eff.getPosition(), 20.0f);
            }
        }
    }

    public void playPositional(Sound sound, Vector2 targetPos, Vector2 sourcePos, float maxDistance) {
        float distance = targetPos.dst(sourcePos);
        float volume = 1 - (distance / maxDistance);
        if (volume < 0) volume = 0;
        float pan = (sourcePos.x - targetPos.x) / maxDistance;
        pan = MathUtils.clamp(pan, -1f, 1f);
        sound.play(volume, 1.0f, pan);
    }

    @Override
    public void dispose() {
        explosion.dispose();
    }
}
