package me.ivy.calc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Storage for calculator named variables.
 */
public class Variables {
    private final Map<String, Double> params = new TreeMap<>();

    /**
     * Defines or overwrites variable value.
     *
     * @param name variable name
     * @param value variable value
     */
    public void define(String name, double value) {
        params.put(name, value);
    }

    /**
     * Checks whether variable exists.
     *
     * @param name variable name
     * @return true when variable exists
     */
    public boolean contains(String name) {
        return params.containsKey(name);
    }

    /**
     * Returns variable value.
     *
     * @param name variable name
     * @return variable value
     * @throws IllegalArgumentException when variable is missing
     */
    public double get(String name) {
        if (!contains(name)) {
            throw new IllegalArgumentException("Unknown variable: " + name);
        }
        return params.get(name);
    }

    /**
     * Returns list of key-value strings suitable for UI display.
     *
     * @return display strings
     */
    public List<String> getDisplayItems() {
        List<String> items = new ArrayList<>();
        for (Map.Entry<String, Double> entry : params.entrySet()) {
            items.add(entry.getKey() + " = " + entry.getValue());
        }
        return items;
    }

    /**
     * Removes all variables.
     */
    public void clear() {
        params.clear();
    }

    /**
     * Returns internal map reference.
     *
     * @return internal mutable map
     */
    public Map<String, Double> asMap() {
        return params;
    }
}
