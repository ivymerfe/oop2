package labs.labgame.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import labs.labgame.model.Enemy;

public class EnemyRenderer implements IRenderer {
    private final Texture texture;

    public EnemyRenderer() {
        texture = new Texture("enemy.png");
    }

    public void render(SpriteBatch batch, Enemy enemy) {
        Vector2 pos = enemy.getPosition();
        float x = pos.x - enemy.getWidth() / 2.0f;
        float y = pos.y - enemy.getHeight() / 2.0f;

        float u1 = enemy.getLookDirection() < 0.0f ? 0.0f : 1.0f;
        float u2 = enemy.getLookDirection() < 0.0f ? 1.0f : 0.0f;
        batch.draw(texture, x, y, enemy.getWidth(), enemy.getHeight(), u1, 1.0f, u2, 0.0f);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}