package jua.util;

import java.util.Arrays;
import java.util.Objects;

public class Pool<E> {

    private static class Entry<E> {

        final int hash;
        final E value;
        final int index;

        Entry<E> left, right;

        Entry(int hash, E value, int index) {
            this.hash = hash;
            this.value = value;
            this.index = index;
        }
    }

    private Entry<E> root = null;

    private int totalIndex = 0;

    public int count() { return totalIndex; }

    public int lookup(E element) {
        int hash = Objects.hashCode(element);
        if (root == null) {
            root = new Entry<>(hash, element, totalIndex++);
            return root.index;
        }
        Entry<E> entry = root;
        while (true) {
            if (hash < entry.hash) {
                if (entry.left == null) {
                    entry.left = new Entry<>(hash, element, totalIndex++);
                    return entry.left.index;
                }
                entry = entry.left;
            } else {
                if (hash == entry.hash && Objects.equals(element, entry.value)) {
                    return entry.index;
                }
                if (entry.right == null) {
                    entry.right = new Entry<>(hash, element, totalIndex++);
                    return entry.right.index;
                }
                entry = entry.right;
            }
        }
    }

    public E[] toArray(E[] a) {
        E[] values;
        if (a.length >= totalIndex) {
            values = a;
        } else {
            values = Arrays.copyOf(a, totalIndex);
        }
        if (root != null) {
            toArray(values, root);
        }
        return values;
    }

    private void toArray(Object[] values, Entry<?> entry) {
        if (entry.left != null) {
            toArray(values, entry.left);
        }
        if (entry.right != null) {
            toArray(values, entry.right);
        }
        values[entry.index] = entry.value;
    }
}
