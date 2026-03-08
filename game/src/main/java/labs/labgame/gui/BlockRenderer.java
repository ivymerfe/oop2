package labs.labgame.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import com.badlogic.gdx.math.Vector2;
import labs.labgame.model.Block;

public class BlockRenderer implements IRenderer {
    private final Texture texture;
    private final int texWidth;
    private final int texHeight;

    public BlockRenderer() {
        texture = new Texture("brick_square.png");
        texWidth = texture.getWidth();
        texHeight = texture.getHeight();
    }

    public void render(SpriteBatch batch, Block block) {
        float size = block.getSize();
        float c = size / 2;
        Vector2 pos = block.getPosition();
        float x = pos.x - c;
        float y = pos.y - c;

        batch.draw(texture, x, y, c, c, size, size,
            1, 1, block.getAngle() * MathUtils.radiansToDegrees,
            0, 0, texWidth, texHeight, false, false);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
