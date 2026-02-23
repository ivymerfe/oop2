package me.ivy.calc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateSerializer {

    public static class CalcState {
        public List<Double> stack;
        public Map<String, Double> variables;

        public CalcState() {
            this.stack = new ArrayList<>();
            this.variables = new HashMap<>();
        }
    }

    public static void saveState(Stack stack, Variables variables, Path filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        // Сохраняем стек
        List<String> stackItems = stack.getDisplayItems();
        for (String item : stackItems) {
            content.append("STACK ").append(item).append("\n");
        }

        // Сохраняем переменные
        List<String> varItems = variables.getDisplayItems();
        for (String item : varItems) {
            String[] parts = item.split(" = ");
            if (parts.length == 2) {
                content.append("VAR ").append(parts[0]).append(" ").append(parts[1]).append("\n");
            }
        }

        Files.write(filePath, content.toString().getBytes());
    }

    public static CalcState loadState(Path filePath) throws IOException {
        CalcState state = new CalcState();

        if (!Files.exists(filePath)) {
            return state;
        }

        List<String> lines = Files.readAllLines(filePath);
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("STACK ")) {
                String value = line.substring(6);
                try {
                    state.stack.add(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            } else if (line.startsWith("VAR ")) {
                String rest = line.substring(4);
                String[] parts = rest.split(" ", 2);
                if (parts.length == 2) {
                    try {
                        String name = parts[0];
                        double value = Double.parseDouble(parts[1]);
                        state.variables.put(name, value);
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
        }

        return state;
    }

    public static void restoreState(Stack stack, Variables variables, CalcState state) {
        for (Double value : state.stack) {
            stack.push(value);
        }
        for (Map.Entry<String, Double> entry : state.variables.entrySet()) {
            variables.define(entry.getKey(), entry.getValue());
        }
    }
}
