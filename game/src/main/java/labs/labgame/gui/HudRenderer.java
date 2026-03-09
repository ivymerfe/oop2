package labs.labgame.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import labs.labgame.model.Player;

public class HudRenderer implements IRenderer {
    private static final float HEART_SIZE = 40.0f;
    private static final float HEART_SPACING = 8.0f;

    private final Texture heartTexture;
    private final BitmapFont font;

    public HudRenderer() {
        heartTexture = new Texture("heart.png");
        font = new BitmapFont(Gdx.files.classpath("a.fnt"), Gdx.files.classpath("a.png"), false);
        font.setColor(Color.WHITE);
    }

    public void render(SpriteBatch batch, Player player) {
        float width = Gdx.graphics.getWidth();

        float baseX = (width / 2 - HEART_SIZE * 10);
        float baseY = 30;
        int health = MathUtils.ceil(player.getHealth());
        int fullHearts = health / 10;
        int remainder = health % 10;

        for (int index = 0; index < fullHearts; index += 1) {
            float x = baseX + index * (HEART_SIZE + HEART_SPACING);
            batch.draw(heartTexture, x, baseY, HEART_SIZE, HEART_SIZE);
        }

        if (remainder > 0) {
            float scale = remainder / 10.0f;
            float size = HEART_SIZE * Math.max(0.35f, scale);
            float x = baseX + fullHearts * (HEART_SIZE + HEART_SPACING) + (HEART_SIZE - size) / 2.0f;
            float y = baseY + (HEART_SIZE - size) / 2.0f;
            batch.draw(heartTexture, x, y, size, size);
        }
        font.draw(batch, "" + player.score, width / 2 + 200, 65);
        font.draw(batch, "" + player.maxScore, width - 100, 65);
    }

    @Override
    public void dispose() {
        heartTexture.dispose();
        font.dispose();
    }
}