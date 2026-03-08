package labs.labgame.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import labs.labgame.model.Player;

public class PlayerRenderer implements IRenderer {
    private final Texture texture;

    public PlayerRenderer() {
        texture = new Texture("player.png");
    }

    public void render(SpriteBatch batch, Player player) {
        float x = player.getX() - player.getWidth() / 2;
        float y = player.getY() - player.getHeight() / 2;

        float u1 = 1;
        float u2 = 0;
        if (player.getLookDirection() < 0) {
            u1 = 0;
            u2 = 1;
        }

        batch.draw(texture, x, y, player.getWidth(), player.getHeight(), u1, 1, u2, 0);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
