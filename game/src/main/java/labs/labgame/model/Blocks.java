package labs.labgame.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blocks {
    public static final float SIZE = 1.2f;

    private final World world;

    private final HashMap<Integer, Body> blocks = new HashMap<>();
    private int nextBlockId = 0;

    public Blocks(World world) {
        this.world = world;
    }

    public int create(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = false;
        bdef.position.set(x, y);
        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(SIZE / 2.0f, SIZE / 2.0f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0.8f;
        fdef.density = 5.0f;
        fdef.restitution = 0.0f;

        Fixture fix = body.createFixture(fdef);
        fix.setUserData(Registry.Block);
        shape.dispose();

        int id = nextBlockId;
        nextBlockId += 1;
        blocks.put(id, body);
        return id;
    }

    public void remove(int id) {
        Body body = blocks.remove(id);
        if (body != null) {
            world.destroyBody(body);
        }
    }

    public List<BlockView> getViews() {
        Collection<Map.Entry<Integer, Body>> entries = blocks.entrySet();
        List<BlockView> result = new ArrayList<>(entries.size());
        for (Map.Entry<Integer, Body> entry : entries) {
            Body body = entry.getValue();
            Vector2 position = body.getPosition();
            result.add(new BlockView(
                    entry.getKey(),
                    position.x,
                    position.y,
                    body.getAngle(),
                    SIZE
            ));
        }
        return result;
    }

    public static class BlockView {
        private final int id;
        private final float x;
        private final float y;
        private final float angle;
        private final float size;

        public BlockView(int id, float x, float y, float angle, float size) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.size = size;
        }

        public int getId() {
            return id;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getAngle() {
            return angle;
        }

        public float getSize() {
            return size;
        }
    }
}
