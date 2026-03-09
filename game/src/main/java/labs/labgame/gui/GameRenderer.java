package labs.labgame.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import labs.labgame.model.*;

public class GameRenderer implements Disposable {
    private final GameModel model;
    private final Camera camera;

    private final SpriteBatch batch;

    private final Texture skyTexture;
    private final Texture groundTexture;

    private final PlayerRenderer playerRenderer;
    private final BlockRenderer blockRenderer;
    private final EnemyRenderer enemyRenderer;
    private final BulletRenderer bulletRenderer;
    private final ExplosionRenderer explosionRenderer;
    private final HudRenderer hudRenderer;
    private final Matrix4 hudProjection;

    public GameRenderer(GameModel model, Camera camera) {
        this.model = model;
        this.camera = camera;

        batch = new SpriteBatch();

        skyTexture = new Texture("sky.png");
        skyTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        groundTexture = new Texture("grass.png");
        groundTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);

        playerRenderer = new PlayerRenderer();
        blockRenderer = new BlockRenderer();
        enemyRenderer = new EnemyRenderer();
        bulletRenderer = new BulletRenderer();
        explosionRenderer = new ExplosionRenderer();
        hudRenderer = new HudRenderer();
        hudProjection = new Matrix4();
    }

    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Vector3 pos = camera.position;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float skyOffset = pos.x * -0.005f;
        batch.draw(skyTexture, pos.x - 16, pos.y - 9, 32, 18, skyOffset, 1, skyOffset - 1, 0);
        float groundOffset = pos.x * -1f/16;
        batch.draw(groundTexture, pos.x - 16, -8, 32, 9, groundOffset, 1, groundOffset - 2, 0);

        for (Entity entity : model.getEntities().values()) {
            if (entity instanceof Block b) blockRenderer.render(batch, b);
            else if (entity instanceof Enemy e) enemyRenderer.render(batch, e);
            else if (entity instanceof Bullet b) bulletRenderer.render(batch, b);
            else if (entity instanceof Player p) playerRenderer.render(batch, p);
        }

        for (Effect effect : model.getEffects()) {
            if (effect instanceof ExplosionEffect e) explosionRenderer.render(batch, e);
        }

        batch.end();

        hudProjection.setToOrtho2D(0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(hudProjection);
        batch.begin();
        hudRenderer.render(batch, model.getPlayer());
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        skyTexture.dispose();
        groundTexture.dispose();
        playerRenderer.dispose();
        blockRenderer.dispose();
        enemyRenderer.dispose();
        bulletRenderer.dispose();
        explosionRenderer.dispose();
        hudRenderer.dispose();
    }
}
