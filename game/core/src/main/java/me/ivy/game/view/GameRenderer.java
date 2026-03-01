package me.ivy.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import me.ivy.game.Labgame;
import org.w3c.dom.Text;

public class GameRenderer implements Disposable {
    Labgame game;

    SpriteBatch batch;
    OrthographicCamera camera;

    Texture skyTexture;
    Texture groundTexture;

    PlayerRenderer playerRenderer;

    public GameRenderer(Labgame game) {
        this.game = game;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        skyTexture = new Texture("sky.png");
        groundTexture = new Texture("ground.jpg");
        groundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        playerRenderer = new PlayerRenderer();
    }

    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Vector2 pos = game.getPlayer().body.getPosition();

        camera.setToOrtho(false, 16, 9);
        camera.translate(0, -1);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(skyTexture, 0, 0, 16, 9);
        batch.draw(groundTexture, 0, -1, 16, 1);

        playerRenderer.draw(batch, game.getPlayer());

        batch.end();
    }

    public void dispose() {
        skyTexture.dispose();
        groundTexture.dispose();
        batch.dispose();
    }
}
