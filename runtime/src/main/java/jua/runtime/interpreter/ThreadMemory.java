package jua.runtime.interpreter;

import jua.runtime.interpreter.memory.Address;
import jua.runtime.interpreter.memory.AddressUtils;

public final class ThreadMemory {

    private Address[] data;

    private int top = 0;

    public ThreadMemory() {
        data = AddressUtils.allocateMemory(32, 0);
    }

    public Address get(int offset) {
        return data[top - offset - 1];
    }

    public void acquire(int capacity) {
        checkAndGrow(capacity);
        top += capacity;
    }

    private void checkAndGrow(int capacity) {
        if (data.length - top < capacity) {
            data = AddressUtils.reallocateWithNewLength(data, (top + capacity) * 2);
        }
    }

    public void release(int capacity) {
        int a = top - capacity;
        for (int t = a; t < top; t++) {
            data[t].reset();
        }
        top = a;
    }
}
