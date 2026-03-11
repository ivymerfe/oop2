package labs.labgame.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import labs.labgame.model.Bullet;
import labs.labgame.model.GameModel;
import labs.labgame.model.Player;

public class GuiInputController {
    private static final float ATTACK_CD = 0.25f;
    private static final float PLACE_CD = 0.1f;

    private final GameModel model;
    private final Camera camera;

    private float lastAttackTime = 0.0f;
    private float lastPlaceTime = 0.0f;

    public GuiInputController(GameModel model, Camera camera) {
        this.model = model;
        this.camera = camera;
    }

    public void update(float delta) {
        Player player = model.getPlayer();

        float direction = 0.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            direction += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            direction -= 1.0f;
        }

        player.setMovement(direction);
        player.setJumping(Gdx.input.isKeyPressed(Input.Keys.SPACE));

        Vector3 point = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.0f);
        camera.unproject(point);
        Vector2 target = new Vector2(point.x, point.y);

        float time = model.getTime();
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && time - lastAttackTime > ATTACK_CD) {
            model.getPlayer().shootAt(target);
            lastAttackTime = time;
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && time - lastPlaceTime > PLACE_CD) {
            model.addBlock(point.x, Math.max(1, point.y));
            lastPlaceTime = time;
        }
    }
}
