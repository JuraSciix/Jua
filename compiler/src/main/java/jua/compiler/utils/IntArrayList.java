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

    public int size() {
        return top;
    }

    public void add(int element) {
        if (top >= elements.length) {
            elements = Arrays.copyOf(elements, elements.length * 2);
        }
        elements[top++] = element;
    }

    public int get(int index) {
        return elements[index];
    }

    public int indexOf(int element) {
        for (int i = 0; i < size(); i++) {
            if (get(i) == element) {
                return i;
            }
        }
        return -1;
    }

    public int[] toArray() {
        return Arrays.copyOfRange(elements, 0, top);
    }
}
