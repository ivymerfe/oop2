package me.ivy.calc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Binary serializer for calculator stack and variables state.
 */
public class StateSerializer {
    private static final int MAGIC = 0x13371337;
    private static final int VERSION = 1;

    /**
     * DTO with stack and variables snapshot.
     */
    public static class CalcState {
        public List<Double> stack;
        public Map<String, Double> variables;

        /**
         * Creates empty state object.
         */
        public CalcState() {
            this.stack = new ArrayList<>();
            this.variables = new HashMap<>();
        }
    }

    /**
     * Saves current calculator state in binary format.
     *
     * @param stack calculator stack
     * @param variables calculator variables
     * @param filePath target file path
     * @throws IOException when write fails
     */
    public static void saveState(Stack stack, Variables variables, Path filePath) throws IOException {
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(filePath)))) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);

            List<Double> stackValues = stack.getValues();
            out.writeInt(stackValues.size());
            for (Double value : stackValues) {
                out.writeDouble(value);
            }

            Map<String, Double> vars = variables.asMap();
            out.writeInt(vars.size());
            for (Map.Entry<String, Double> entry : vars.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeDouble(entry.getValue());
            }
        }
    }

    /**
     * Loads calculator state from binary file.
     *
     * @param filePath source file path
     * @return loaded state, or empty state if file does not exist
     * @throws IOException when read fails or format is invalid
     */
    public static CalcState loadState(Path filePath) throws IOException {
        CalcState state = new CalcState();

        if (!Files.exists(filePath)) {
            return state;
        }

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(filePath)))) {
            int magic = in.readInt();
            if (magic != MAGIC) {
                throw new IOException("Invalid state file format");
            }

            int version = in.readInt();
            if (version != VERSION) {
                throw new IOException("Unsupported state file version: " + version);
            }

            int stackSize = in.readInt();
            if (stackSize < 0) {
                throw new IOException("Invalid stack size in state file");
            }
            for (int i = 0; i < stackSize; i++) {
                state.stack.add(in.readDouble());
            }

            int varsSize = in.readInt();
            if (varsSize < 0) {
                throw new IOException("Invalid variables size in state file");
            }
            for (int i = 0; i < varsSize; i++) {
                String name = in.readUTF();
                double value = in.readDouble();
                state.variables.put(name, value);
            }
        } catch (EOFException e) {
            throw new IOException("Corrupted state file", e);
        }

        return state;
    }

    /**
     * Restores state into provided calculator storage objects.
     *
     * @param stack destination stack
     * @param variables destination variables
     * @param state source state
     */
    public static void restoreState(Stack stack, Variables variables, CalcState state) {
        for (int i = state.stack.size() - 1; i >= 0; i--) {
            stack.push(state.stack.get(i));
        }
        for (Map.Entry<String, Double> entry : state.variables.entrySet()) {
            variables.define(entry.getKey(), entry.getValue());
        }
    }
}
