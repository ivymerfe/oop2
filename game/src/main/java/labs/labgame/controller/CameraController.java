package labs.labgame.controller;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import labs.labgame.model.GameModel;

public class CameraController {
    private final GameModel model;
    private final Camera camera;

    public CameraController(GameModel model, Camera camera) {
        this.model = model;
        this.camera = camera;
    }

    public void update(float deltaTime) {
        float lerp = 0.1f;
        Vector3 pos = camera.position;
        float targetX = model.getPlayer().getX();
        float targetY = model.getPlayer().getY() + 2;
        pos.x += (targetX - pos.x) * lerp;
        pos.y += (targetY - pos.y) * lerp;

        camera.update();
    }
}

