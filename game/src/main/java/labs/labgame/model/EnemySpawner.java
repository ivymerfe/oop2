package labs.labgame.model;

import java.util.Random;

public class EnemySpawner {
    private static final float INITIAL_SPAWN_DELAY = 2.0f;
    private static final float BASE_SPAWN_INTERVAL = 2.0f;
    private static final float MIN_SPAWN_INTERVAL = 1.0f;
    private static final int MAX_ENEMIES = 8;
    private static final float SPAWN_DISTANCE_MIN = 12.0f;
    private static final float SPAWN_DISTANCE_MAX = 22.0f;
    private static final float SPAWN_HEIGHT = 2.5f;

    private final GameModel model;
    private final Random random;

    private float nextSpawnTime;

    public EnemySpawner(GameModel model) {
        this.model = model;
        this.random = new Random();
        this.nextSpawnTime = INITIAL_SPAWN_DELAY;
    }

    public void update() {
        int aliveEnemies = model.countAliveEnemies();
        int targetEnemies = Math.min(MAX_ENEMIES, 2 + model.getPlayer().score / 2);

        if (aliveEnemies >= targetEnemies || model.getTime() < nextSpawnTime) {
            return;
        }

        while (aliveEnemies < targetEnemies && model.getTime() >= nextSpawnTime) {
            if (trySpawnEnemy()) {
                aliveEnemies += 1;
            }
            nextSpawnTime += getSpawnInterval();
        }
    }

    public void reset() {
        nextSpawnTime = model.getTime() + INITIAL_SPAWN_DELAY;
    }

    private float getSpawnInterval() {
        float difficultyFactor = model.getPlayer().score * 0.1f + model.getTime() * 0.015f;
        return Math.max(MIN_SPAWN_INTERVAL, BASE_SPAWN_INTERVAL - difficultyFactor);
    }

    private boolean trySpawnEnemy() {
        for (int attempt = 0; attempt < 8; attempt++) {
            float direction = random.nextBoolean() ? 1.0f : -1.0f;
            float distance = SPAWN_DISTANCE_MIN + random.nextFloat() * (SPAWN_DISTANCE_MAX - SPAWN_DISTANCE_MIN);
            float spawnX = model.getPlayer().getPosition().x + direction * distance;
            float spawnY = SPAWN_HEIGHT + random.nextFloat() * 2.5f;

            if (model.isSpawnAreaClear(spawnX, spawnY)) {
                model.addEnemy(spawnX, spawnY);
                return true;
            }
        }
        return false;
    }
}