package me.ivy.game.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import me.ivy.game.Labgame;
import me.ivy.game.model.Ground;
import me.ivy.game.model.Player;

public class InputController {
    private Labgame game;

    public InputController(Labgame game) {
        this.game = game;
    }

    public void update(float delta) {
        game.getWorld().step(delta, 6, 2);

        // Ввод
        float direction = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.D))  {
            direction += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            direction -= 1;
        }
        if (direction != 0) {
            game.getPlayer().move(direction);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.getPlayer().jump();
        }
    }
}
