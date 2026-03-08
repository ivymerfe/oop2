package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.*;

public class GameModel {
    private final World world;
    private final Player player;
    private final Ground ground;
    private final EnemySpawner enemySpawner;

    private final Map<Integer, Entity> entities;
    private final List<Entity> pendingEntities;
    private final List<Effect> effects;

    private float time;
    private boolean updating;

    public GameModel() {
        world = new World(new Vector2(0.0f, -40.0f), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Entity entityA = getEntityOfFixture(fixtureA);
                Entity entityB = getEntityOfFixture(fixtureB);
                Object udA = fixtureA.getUserData();
                Object udB = fixtureB.getUserData();

                if (entityA != null) entityA.onCollisionEnter(entityB, udA);
                if (entityB != null) entityB.onCollisionEnter(entityA, udB);
            }

            @Override
            public void endContact(Contact contact) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Entity entityA = getEntityOfFixture(fixtureA);
                Entity entityB = getEntityOfFixture(fixtureB);

                if (entityA != null) entityA.onCollisionExit(entityB, fixtureA.getUserData());
                if (entityB != null) entityB.onCollisionExit(entityA, fixtureB.getUserData());
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });

        player = new Player(this);
        ground = new Ground(world);
        player.persistent = true;
        ground.persistent = true;

        entities = new HashMap<>();
        pendingEntities = new ArrayList<>();
        effects = new ArrayList<>();
        time = 0.0f;
        updating = false;
        enemySpawner = new EnemySpawner(this);

        addEntity(player);
        addEntity(ground);
    }

    private static Entity getEntityOfFixture(Fixture fixture) {
        Object o = fixture.getBody().getUserData();
        return (o instanceof Entity e) ? e : null;
    }

    public void addEntity(Entity entity) {
        if (updating) {
            pendingEntities.add(entity);
            return;
        }
        entities.put(entity.getId(), entity);
    }

    public void addEffect(ExplosionEffect effect) {
        effects.add(effect);
    }

    public void addBlock(float x, float y) {
        addEntity(new Block(world, x, y));
    }

    public void addEnemy(float x, float y) {
        addEntity(new Enemy(this, x, y));
    }

    public void addBullet(Entity owner, Vector2 spawn, Vector2 target, Vector2 initialVelocity) {
        Vector2 direction = target.sub(spawn);
        if (direction.isZero(0.001f)) {
            float fallback = owner instanceof Player p ? p.getLookDirection() : ((Enemy) owner).getLookDirection();
            direction.set(fallback, 0.0f);
        }
        direction.nor();
        addEntity(new Bullet(this, owner, spawn.x, spawn.y, direction, initialVelocity));
    }

    public void addExplosion(Vector2 center, float radius, float power, float damage) {
        addEffect(new ExplosionEffect(center, radius));

        float queryDist = 4 * radius;
        world.QueryAABB(fixture -> {
            Body body = fixture.getBody();
            Vector2 bodyPosition = body.getWorldCenter();
            float distance = Math.max(0, center.dst(bodyPosition) - radius);
            if (distance <= radius) {
                Object o = body.getUserData();
                if (o instanceof Entity e) {
                    e.damage(damage / (radius + distance));
                }
                Vector2 direction = bodyPosition.cpy().sub(center).nor();
                Vector2 impulse = direction.scl(power*(1 - distance / radius));
                body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
            }
            return true;
        }, center.x - queryDist, center.y - queryDist, center.x + queryDist, center.y + queryDist);
    }

    public void update(float delta) {
        time += delta;
        enemySpawner.update();

        updating = true;
        for (Entity entity : entities.values()) {
            entity.update(delta);
        }
        updating = false;
        spawnEntitiesPending();

        world.step(delta, 6, 2);
        effects.removeIf(effect -> effect.update(delta));
        cleanupEntities();
    }

    private void spawnEntitiesPending() {
        if (pendingEntities.isEmpty()) {
            return;
        }
        for (Entity entity : pendingEntities) {
            entities.put(entity.getId(), entity);
        }
        pendingEntities.clear();
    }

    int countAliveEnemies() {
        int count = 0;
        for (Entity entity : entities.values()) {
            if (entity instanceof Enemy enemy && !enemy.isRemoved()) {
                count += 1;
            }
        }
        for (Entity entity : pendingEntities) {
            if (entity instanceof Enemy enemy && !enemy.isRemoved()) {
                count += 1;
            }
        }
        return count;
    }

    boolean isSpawnAreaClear(float x, float y) {
        final boolean[] blocked = {false};
        float halfWidth = Enemy.WIDTH * 0.75f;
        float halfHeight = Enemy.HEIGHT * 0.75f;

        world.QueryAABB(fixture -> {
            Object hit = fixture.getBody().getUserData();
            if (hit instanceof Entity entity && !entity.isRemoved()) {
                blocked[0] = true;
                return false;
            }
            return true;
        }, x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight);

        return !blocked[0];
    }

    private void cleanupEntities() {
        Iterator<Map.Entry<Integer, Entity>> it = entities.entrySet().iterator();
        while (it.hasNext()) {
            Entity entity = it.next().getValue();
            if (entity.isRemoved()) {
                world.destroyBody(entity.getBody());
                it.remove();
            }
        }
    }

    public void resetEntities() {
        entities.values().forEach(e -> {
            if (!e.persistent) e.remove();
        });
        pendingEntities.clear();
        enemySpawner.reset();
    }

    public void dispose() {
        world.dispose();
    }

    public float getTime() {
        return time;
    }

    public World getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }

    public Map<Integer, Entity> getEntities() {
        return entities;
    }

    public List<Effect> getEffects() {
        return effects;
    }
}
