package labs.labgame.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import labs.labgame.model.ExplosionEffect;

public class ExplosionRenderer implements IRenderer {
    private Texture tex;
    private Animation<TextureRegion> anim;

    public ExplosionRenderer() {
        int rows = 8;
        int cols = 8;
        tex = new Texture(Gdx.files.classpath("explosion.png"));
        TextureRegion[][] tmp = TextureRegion.split(tex, tex.getWidth() / rows, tex.getHeight() / cols);
        TextureRegion[] frames = new TextureRegion[rows * cols];
        int index = 0;
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        anim = new Animation<>(1f / (rows * cols), frames);
    }

    public void render(SpriteBatch batch, ExplosionEffect effect) {
        TextureRegion frame = anim.getKeyFrame(effect.getProgress());
        float radius = effect.getRadius();
        float diameter = 2 * radius;
        batch.draw(frame,
                effect.getPosition().x - radius,
                effect.getPosition().y - radius,
                diameter, diameter);
    }

    @Override
    public void dispose() {
        tex.dispose();
    }
}
