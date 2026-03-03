package me.ivy.game.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import me.ivy.game.Labgame;
import me.ivy.game.model.Player;

public class InputController {
    public static final float PLACE_CD = 0.4f;

    private Labgame game;

    private float lastPlaceTime = 0;

    public InputController(Labgame game) {
        this.game = game;
    }

    public void update(float delta) {
        Player player = game.getPlayer();

        float direction = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            direction += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            direction -= 1;
        }
        player.movement = direction;
        if (direction != 0) {
            player.lookDirection = direction;
        }
        player.jumping = Gdx.input.isKeyPressed(Input.Keys.SPACE);


        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            Vector3 point = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.getCamera().unproject(point);

            float time = game.getTime();
            if (time - lastPlaceTime > PLACE_CD) {
                game.getBlocks().create(point.x, point.y);

                lastPlaceTime = time;
            }
        }
    }
}
