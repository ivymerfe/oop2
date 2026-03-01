package me.ivy.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
        camera.setToOrtho(false, 32, 18);

        skyTexture = new Texture("sky.png");
        groundTexture = new Texture("ground.jpg");
        groundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        playerRenderer = new PlayerRenderer();
    }

    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float lerp = 0.1f; // Коэффициент плавности (чем меньше, тем медленнее догоняет)

        Vector3 pos = camera.position;
        float targetX = game.getPlayer().body.getPosition().x;
        float targetY = 6;
        pos.x += (targetX - pos.x) * lerp;
        pos.y += (targetY - pos.y) * lerp;

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(skyTexture, pos.x - 16, pos.y - 9, 32, 18);
        float offset = pos.x * -0.02f;
        batch.draw(groundTexture, pos.x - 16, pos.y - 9, 32, 4, offset, 1, offset - 1, 0);

        playerRenderer.draw(batch, game.getPlayer());

        batch.end();
    }

    public void dispose() {
        skyTexture.dispose();
        groundTexture.dispose();
        batch.dispose();
    }
}
