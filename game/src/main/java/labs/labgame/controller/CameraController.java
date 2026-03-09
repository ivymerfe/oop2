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
        float lerpX = 0.1f;
        float lerpY = 0.05f;
        Vector3 pos = camera.position;
        float targetX = model.getPlayer().getX();
        float targetY = model.getPlayer().getY() + 1;
        pos.x += (targetX - pos.x) * lerpX;
        pos.y += (targetY - pos.y) * lerpY;

        camera.update();
    }
}

