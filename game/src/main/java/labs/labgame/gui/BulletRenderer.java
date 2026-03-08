package labs.labgame.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import labs.labgame.model.Bullet;

public class BulletRenderer implements IRenderer {
    private Texture tex;
    private Animation<TextureRegion> anim;

    public BulletRenderer() {
        int rows = 8;
        int cols = 7;
        tex = new Texture(Gdx.files.classpath("fireball.png"));
        TextureRegion[][] tmp = TextureRegion.split(tex, tex.getWidth() / rows, tex.getHeight() / cols);
        int num_frames = rows*(cols-4);
        TextureRegion[] frames = new TextureRegion[num_frames];
        int index = 0;
        for (int i = 2; i < cols - 2; i++) {
            for (int j = 0; j < rows; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        anim = new Animation<>(1.0f/num_frames, frames);
    }

    public void render(SpriteBatch batch, Bullet bullet) {
        float radius = Bullet.RADIUS;
        float diameter = radius * 2;
        Vector2 pos = bullet.getPosition();
        TextureRegion frame = anim.getKeyFrame(bullet.getLifeTime(), true);
        batch.draw(frame, pos.x - radius, pos.y - radius, diameter, diameter);
    }

    @Override
    public void dispose() {
        tex.dispose();
    }
}