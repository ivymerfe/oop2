package me.ivy.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import me.ivy.game.Labgame;

public class GameRenderer {
    Labgame game;

    SpriteBatch batch;

    Texture skyTexture;
    Texture groundTexture;

    PlayerRenderer playerRenderer;
    BlockRenderer blockRenderer;

    public GameRenderer(Labgame game) {
        this.game = game;

        batch = new SpriteBatch();

        skyTexture = new Texture("sky.png");
        groundTexture = new Texture("ground.jpg");
        groundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        playerRenderer = new PlayerRenderer();
        blockRenderer = new BlockRenderer();
    }

    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Camera camera = game.getCamera();
        Vector3 pos = camera.position;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(skyTexture, pos.x - 16, pos.y - 9, 32, 18);
        float offset = pos.x * -0.02f;
        batch.draw(groundTexture, pos.x - 16, pos.y - 9, 32, 4, offset, 1, offset - 1, 0);

        playerRenderer.render(batch, game.getPlayer());
        blockRenderer.render(batch, game.getBlocks());

        batch.end();
    }
}
