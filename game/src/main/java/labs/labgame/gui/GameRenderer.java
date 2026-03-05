package labs.labgame.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import labs.labgame.model.GameModel;

public class GameRenderer implements Disposable {
    private final GameModel model;
    private final Camera camera;

    private final SpriteBatch batch;

    private final Texture skyTexture;
    private final Texture groundTexture;

    private final PlayerRenderer playerRenderer;
    private final BlockRenderer blockRenderer;

    public GameRenderer(GameModel model, Camera camera) {
        this.model = model;
        this.camera = camera;

        batch = new SpriteBatch();

        skyTexture = new Texture("sky.png");
        skyTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        groundTexture = new Texture("ground.jpg");
        groundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        playerRenderer = new PlayerRenderer();
        blockRenderer = new BlockRenderer();
    }

    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Vector3 pos = camera.position;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float skyOffset = pos.x * -0.005f;
        batch.draw(skyTexture, pos.x - 16, pos.y - 9, 32, 18, skyOffset, 1, skyOffset - 1, 0);
        float groundOffset = pos.x * -0.02f;
        batch.draw(groundTexture, pos.x - 16, pos.y - 9, 32, 4, groundOffset, 1, groundOffset - 1, 0);

        playerRenderer.render(batch, model.getPlayer());
        blockRenderer.render(batch, model.getBlocks());

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        skyTexture.dispose();
        groundTexture.dispose();
        playerRenderer.dispose();
        blockRenderer.dispose();
    }
}
