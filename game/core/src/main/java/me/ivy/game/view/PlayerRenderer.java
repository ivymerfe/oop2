package me.ivy.game.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.ivy.game.model.Player;

public class PlayerRenderer {
    private Texture texture;

    public PlayerRenderer() {
        texture = new Texture("player.png");
    }

    public void render(SpriteBatch batch, Player player) {
        float x = (player.body.getPosition().x - player.width / 2);
        float y = (player.body.getPosition().y - player.height / 2);

        float u1 = 1;
        float u2 = 0;
        if (player.lookDirection < 0) {
            u1 = 0;
            u2 = 1;
        }

        batch.draw(texture, x, y, player.width, player.height, u1, 1, u2, 0);
    }
}
