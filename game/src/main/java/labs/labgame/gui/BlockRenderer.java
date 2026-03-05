package labs.labgame.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import labs.labgame.model.Blocks;

public class BlockRenderer implements Disposable {
    private final Texture texture;
    private final int texWidth;
    private final int texHeight;

    public BlockRenderer() {
        texture = new Texture("brick_square.png");
        texWidth = texture.getWidth();
        texHeight = texture.getHeight();
    }

    public void render(SpriteBatch batch, Blocks blocks) {
        for (Blocks.BlockView block : blocks.getViews()) {
            float size = block.getSize();
            float c = size / 2;
            float x = block.getX() - c;
            float y = block.getY() - c;

            batch.draw(texture, x, y, c, c, size, size,
                1, 1, block.getAngle() * MathUtils.radiansToDegrees,
                0, 0, texWidth, texHeight, false, false);
        }
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
