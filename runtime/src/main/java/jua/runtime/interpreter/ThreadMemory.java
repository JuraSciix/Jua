package jua.runtime.interpreter;

import java.util.Objects;

public final class ThreadMemory {

    private Address[] data;

    private int top = 0;

    private InterpreterFrame cFrame;

    public ThreadMemory() {
        data = AddressUtils.allocateMemory(32, 0);
    }

    public void setCurrentFrame(InterpreterFrame frame) {
        cFrame = Objects.requireNonNull(frame);
    }

    public Address get(int offset) {
        return data[cFrame.getRegBase() + offset];
    }

    public Address getShared(int offset) {
        return null;
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

    public void debug() {
        System.out.println("Memory used: " + top);
        System.out.print(" > ");
        for (int i = 0; i < top; i++) {
            System.out.printf("%-2s ", data[i].getTypeName().charAt(0));
        }
        System.out.println();
        System.out.print(" > ");
        for (int i = 0; i < top; i++) {
            System.out.printf("%02x ", i);
        }
        System.out.println();
    }
}
