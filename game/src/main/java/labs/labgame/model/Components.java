package labs.labgame.model;

import com.badlogic.gdx.physics.box2d.Body;

import java.util.HashMap;

public class Components {

    public HashMap<Integer, Body> bodies;
    public HashMap<Integer, Integer> health;
    public HashMap<Integer, Integer> bulletPower;
}
