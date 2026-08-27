package jua.vm.arena;

import java.util.Arrays;

public final class ReferenceArena {
    private static final int INITIAL_CAPACITY = 512;

    private final IntArrayQueue stock;
    private Object[] data;

    public ReferenceArena() {
        stock = new IntArrayQueue(INITIAL_CAPACITY);
        data = new Object[INITIAL_CAPACITY];
        for (int i = 0; i < INITIAL_CAPACITY; i++) {
            stock.addLast(i);
        }
    }

    public int put(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        if (stock.isEmpty()) {
            doubleCapacity();
        }

        int handle = stock.pollFirst();
        data[handle] = object;
        return handle;
    }

    private void doubleCapacity() {
        int cap1 = data.length;
        int cap2 = cap1 * 2;
        data = Arrays.copyOf(data, cap2);
        for (int i = cap1; i < cap2; i++)
            stock.addLast(i);
    }

    public void delete(int handle) {
        if (data[handle] != null) {
            data[handle] = null;
            stock.addLast(handle);
        }
    }

    public Object read(int handle) {
        if (data[handle] != null) {
            return data[handle];
        }
        throw new IllegalArgumentException("Handle is not initialized");
    }
}
