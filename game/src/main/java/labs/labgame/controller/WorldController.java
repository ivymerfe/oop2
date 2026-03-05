package labs.labgame.controller;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import labs.labgame.model.GameModel;
import labs.labgame.model.Player;
import labs.labgame.model.Registry;

public class WorldController {
    private final GameModel model;
    private final Camera camera;
    private final Player player;
    final ContactChecker contactChecker;

    public WorldController(GameModel model, Camera camera) {
        this.model = model;
        this.camera = camera;
        this.player = model.getPlayer();
        contactChecker = new ContactChecker();
        model.getWorld().setContactListener(contactChecker);
    }

    public void update(float deltaTime) {
        model.update(deltaTime);

        float lerp = 0.1f;
        Vector3 pos = camera.position;
        float targetX = player.getX();
        float targetY = 6;
        pos.x += (targetX - pos.x) * lerp;
        pos.y += (targetY - pos.y) * lerp;

        camera.update();
    }

    class ContactChecker implements ContactListener {
        @Override
        public void beginContact(Contact contact) {
            Object u1 = contact.getFixtureA().getUserData();
            Object u2 = contact.getFixtureB().getUserData();
            if (Registry.PlayerFoot.equals(u1) || Registry.PlayerFoot.equals(u2)) {
                player.incrementFootContacts();
            }
            if (Registry.Player.equals(u1) || Registry.Player.equals(u2)) {
                player.incrementContacts();
            }
        }

        @Override
        public void endContact(Contact contact) {
            Object u1 = contact.getFixtureA().getUserData();
            Object u2 = contact.getFixtureB().getUserData();
            if (Registry.PlayerFoot.equals(u1) || Registry.PlayerFoot.equals(u2)) {
                player.decrementFootContacts();
            }
            if (Registry.Player.equals(u1) || Registry.Player.equals(u2)) {
                player.decrementContacts();
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
        }
    }
}
