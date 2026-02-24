package me.ivy.calc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Stack of numeric values used by calculator commands.
 */
public class Stack {
    private final Deque<Double> values = new ArrayDeque<>();

    /**
     * Pushes value to stack top.
     *
     * @param value value to push
     */
    public void push(double value) {
        values.push(value);
    }

    /**
     * Pops top value from stack.
     *
     * @return removed top value
     * @throws IllegalStateException when stack is empty
     */
    public double pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return values.pop();
    }

    /**
     * Returns top value without removal.
     *
     * @return top value
     * @throws IllegalStateException when stack is empty
     */
    public double peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return values.peek();
    }

    /**
     * Checks whether stack is empty.
     *
     * @return true when empty
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Returns current number of values.
     *
     * @return stack size
     */
    public int size() {
        return values.size();
    }

    /**
     * Returns string representation of values in stack iteration order.
     *
     * @return list of values as strings
     */
    public List<String> getDisplayItems() {
        List<String> items = new ArrayList<>();
        for (Double value : values) {
            items.add(String.valueOf(value));
        }
        return items;
    }

    /**
     * Returns copy of current stack values in stack iteration order.
     *
     * @return copied list of numeric values
     */
    public List<Double> getValues() {
        return new ArrayList<>(values);
    }

    /**
     * Removes all values from stack.
     */
    public void clear() {
        values.clear();
    }
}
