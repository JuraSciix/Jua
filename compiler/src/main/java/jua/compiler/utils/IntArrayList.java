package jua.compiler.utils;

import java.util.Arrays;

public class IntArrayList {

    private int[] elements;

    private int top = 0;

    public IntArrayList() {
        this(16);
    }

    public IntArrayList(int initialCapacity) {
        elements = new int[initialCapacity];
    }

    public void add(int element) {
        if (top >= elements.length) {
            elements = Arrays.copyOf(elements, elements.length * 2);
        }
        elements[top++] = element;
    }

    public int[] toArray() {
        return Arrays.copyOfRange(elements, 0, top);
    }
}
