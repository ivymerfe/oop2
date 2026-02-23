package me.ivy.calc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Stack {
    private final Deque<Double> values = new ArrayDeque<>();

    public void push(double value) {
        values.push(value);
    }

    public double pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return values.pop();
    }

    public double peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return values.peek();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public int size() {
        return values.size();
    }

    public List<String> getDisplayItems() {
        List<String> items = new ArrayList<>();
        for (Double value : values) {
            items.add(String.valueOf(value));
        }
        return items;
    }

    public void clear() {
        values.clear();
    }
}
