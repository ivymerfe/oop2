package me.ivy.game.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import me.ivy.game.model.Blocks;

import java.util.Map;

public class BlockRenderer {
    private Texture texture;
    private int texWidth;
    private int texHeight;

    public BlockRenderer() {
        texture = new Texture("brick_square.png");
        texWidth = texture.getWidth();
        texHeight = texture.getHeight();
    }

    public void render(SpriteBatch batch, Blocks blocks) {
        for (Map.Entry<Integer, Body> entry : blocks.getBlocks().entrySet()) {
            Body body = entry.getValue();
            Vector2 pos = body.getPosition();
            float angle = body.getAngle();

            float size = Blocks.SIZE;
            float c = size / 2;
            float x = (pos.x - c);
            float y = (pos.y - c);

            batch.draw(texture, x, y, c, c, size, size,
                1, 1, angle * MathUtils.radiansToDegrees,
                0, 0, texWidth, texHeight, false, false);
        }
    }
}
