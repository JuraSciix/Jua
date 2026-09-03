package jua.compiler.utils;

import java.util.Arrays;

public final class IntArrayList {
    private static final int INITIAL_CAPACITY = 16;

    private int[] array;
    private int count = 0;

    public IntArrayList() {
        this(INITIAL_CAPACITY);
    }

    public IntArrayList(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Negative capacity");
        }
        array = new int[capacity];
    }

    public int size() {
        return count;
    }

    public void add(int element) {
        if (count >= array.length) {
            // На практике newCapacity никогда даже не приблизится к Integer.MAX_VALUE
            int newCapacity = Math.max(array.length, INITIAL_CAPACITY / 2) * 2;
            array = Arrays.copyOf(array, newCapacity);
        }
        array[count++] = element;
    }

    public int get(int index) {
        if (0 <= index && index < count) {
            return array[index];
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    public void set(int index, int value) {
        if (0 <= index && index < count) {
            array[index] = value;
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    public void and(int index, int mask) {
        if (0 <= index && index < count) {
            array[index] &= mask;
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    public void or(int index, int mask) {
        if (0 <= index && index < count) {
            array[index] |= mask;
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    private String oob(int index) {
        return "Index: " + index + ". Count: " + count;
    }

    public int[] toArray() {
        return Arrays.copyOf(array, count);
    }
}
