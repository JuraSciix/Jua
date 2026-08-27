package jua;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CodeBuilder {
    private static final int INITIAL_CAPACITY = 256;

    private final Map<Object, Integer> jumps = new HashMap<>();
    private int[] array = new int[INITIAL_CAPACITY];
    private int cp = 0;

    public CodeBuilder emit(int opcode) {
        return emit(opcode, 0);
    }

    public CodeBuilder emit(int opcode, int payload) {
        array[cp * 2] = opcode;
        array[cp * 2 + 1] = payload;
        cp++;
        return this;
    }

    public CodeBuilder emitJump(int opcode, Object handle) {
        if (handle == null) {
            throw new NullPointerException("Handle is null");
        }
        if (jumps.containsKey(handle)) {
            emit(opcode, jumps.get(handle));
        } else {
            jumps.put(handle, cp);
            emit(opcode);
        }
        return this;
    }

    public CodeBuilder resolveJump(Object handle) {
        if (handle == null) {
            throw new NullPointerException("Handle is null");
        }
        if (jumps.containsKey(handle)) {
            int pp = jumps.get(handle);
            array[pp * 2 + 1] = cp;
        }
        jumps.put(handle, cp);
        return this;
    }

    public int[] toArray() {
        return Arrays.copyOf(array, cp * 2 + 1);
    }
}
