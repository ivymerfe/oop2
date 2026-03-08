package labs.labgame.controller;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import labs.labgame.model.GameModel;

public class GameController {
    private final GameModel model;
    private final Camera camera;

    public GameController(GameModel model, Camera camera) {
        this.model = model;
        this.camera = camera;
    }

    public void update(float deltaTime) {
        model.update(deltaTime);

        float lerp = 0.1f;
        Vector3 pos = camera.position;
        float targetX = model.getPlayer().getX();
        float targetY = 6;
        pos.x += (targetX - pos.x) * lerp;
        pos.y += (targetY - pos.y) * lerp;

        camera.update();
    }
}

