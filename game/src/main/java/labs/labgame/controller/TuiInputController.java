package labs.labgame.controller;

import com.badlogic.gdx.math.Vector2;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.MouseAction;
import com.googlecode.lanterna.input.MouseActionType;
import labs.labgame.model.GameModel;
import labs.labgame.model.Player;
import labs.labgame.tui.Main;
import labs.labgame.tui.TuiRenderer;

public class TuiInputController {
    private static final float ATTACK_CD = 0.25f;
    private static final float PLACE_CD = 0.1f;
    private static final float HOLD = 0.3f;

    private final GameModel model;

    private float lastAttackTime = -ATTACK_CD;
    private float lastPlaceTime = -PLACE_CD;
    private float leftUntil = 0;
    private float rightUntil = 0;
    private float jumpUntil = 0;
    private boolean attack = false;
    private boolean place = false;
    private boolean quitRequested = false;
    private int col;
    private int row;

    public TuiInputController(GameModel model) {
        this.model = model;
    }

    public void handleInput(KeyStroke keyStroke) {
        if (keyStroke == null) {
            return;
        }
        if (keyStroke instanceof MouseAction mouseAction) {
            int col = mouseAction.getPosition().getColumn();
            int row = mouseAction.getPosition().getRow();
            int button = mouseAction.getButton();
            MouseActionType type = mouseAction.getActionType();
            if (type == MouseActionType.CLICK_DOWN) {
                if (button == 1) attack = true;
                if (button == 3) place = true;
            }
            if (type == MouseActionType.CLICK_RELEASE) {
                attack = false;
                place = false;
            }
            if (col != 0 && row != 0) {
                this.col = col;
                this.row = row;
            }
            return;
        }
        float time = model.getTime();
        float cont = time + HOLD;
        Character character = keyStroke.getCharacter();
        switch (Character.toLowerCase(character)) {
            case 'a' -> leftUntil = cont;
            case 'd' -> rightUntil = cont;
            case 'w', ' ' -> {
                if (time < leftUntil) leftUntil = cont;
                if (time < rightUntil) rightUntil = cont;
                jumpUntil = cont;
            }
            case 'q' -> quitRequested = true;
            default -> {
            }
        }
    }

    public void update(TuiRenderer.Viewport viewport) {
        Player player = model.getPlayer();
        float time = model.getTime();

        float direction = 0;
        if (time < leftUntil) direction -= 1;
        if (time < rightUntil) direction += 1;

        player.setMovement(direction);
        player.setJumping(time < jumpUntil);

        Vector2 target;
        if (!viewport.contains(col, row)) {
            return;
        }
        target = viewport.cellCenterToWorld(col, row);

        if (attack && time - lastAttackTime >= ATTACK_CD) {
            Vector2 bulletPos = player.getPosition().cpy().sub(target).nor().scl(-2.0f).add(player.getPosition());
            if (bulletPos.y < 0.0f) {
                bulletPos.y = 0.0f;
            }

            model.addBullet(player, bulletPos, target, player.getBody().getLinearVelocity().cpy());
            lastAttackTime = time;
        }
        if (place && time - lastPlaceTime >= PLACE_CD) {
            model.addBlock(target.x, Math.max(1.0f, target.y));
            lastPlaceTime = time;
        }
    }

    public boolean isQuitRequested() {
        return quitRequested;
    }
}