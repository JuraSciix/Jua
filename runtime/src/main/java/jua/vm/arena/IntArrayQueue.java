package jua.vm.arena;

import java.util.NoSuchElementException;

public final class IntArrayQueue {
    private static final int INITIAL_CAPACITY = 16;

    private int[] elements;
    private int mask;
    private int head;
    private int tail;

    public IntArrayQueue() {
        this(INITIAL_CAPACITY);
    }

    public IntArrayQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        int hob = Integer.highestOneBit(capacity - 1) << 1;
        elements = new int[hob];
        mask = hob - 1;
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public void addLast(int e) {
        elements[tail] = e;
        tail = (tail + 1) & mask;
        if (tail == head) {
            doubleCapacity();
        }
    }

    public int pollFirst() {
        if (tail == head) {
            throw new NoSuchElementException("Queue is empty");
        }
        int result = elements[head];
        head = (head + 1) & mask;
        return result;
    }

    private void doubleCapacity() {
        int cap = elements.length;
        int[] newElements = new int[cap * 2];
        // Выпрямляем массив
        System.arraycopy(elements, head, newElements, 0, cap - head);
        System.arraycopy(elements, 0, newElements, cap - head, tail);
        elements = newElements;
        head = 0;
        tail = cap;
        mask = (mask << 1) | 1;
    }
}
