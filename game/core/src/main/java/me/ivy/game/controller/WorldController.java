package me.ivy.game.controller;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import me.ivy.game.Labgame;
import me.ivy.game.model.Player;
import me.ivy.game.model.Registry;

public class WorldController {
    final Labgame game;
    final ContactChecker contactChecker;

    public WorldController(Labgame game) {
        this.game = game;
        contactChecker = new ContactChecker();
        game.getWorld().setContactListener(contactChecker);
    }

    public void update(float deltaTime) {
        game.getWorld().step(deltaTime, 6, 2);

        Camera camera = game.getCamera();
        float lerp = 0.1f;
        Vector3 pos = camera.position;
        float targetX = game.getPlayer().body.getPosition().x;
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
                game.getPlayer().footContacts += 1;
            }
            if (Registry.Player.equals(u1) || Registry.Player.equals(u2)) {
                game.getPlayer().contacts += 1;
            }
        }

        @Override
        public void endContact(Contact contact) {
            Object u1 = contact.getFixtureA().getUserData();
            Object u2 = contact.getFixtureB().getUserData();
            if (Registry.PlayerFoot.equals(u1) || Registry.PlayerFoot.equals(u2)) {
                game.getPlayer().footContacts -= 1;
            }
            if (Registry.Player.equals(u1) || Registry.Player.equals(u2)) {
                game.getPlayer().contacts -= 1;
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
