package jua.compiler;

import java.util.Objects;

public class ConstantPoolWriter {

    private static class Entry {

        final int hash;
        final Object value;
        final int index;

        Entry left, right;

        Entry(int hash, Object value, int index) {
            this.hash = hash;
            this.value = value;
            this.index = index;
        }
    }

    private Entry root = null;

    private int totalIndex = 0;

    public int writeLong(long l) { return write(l); }

    public int writeDouble(double d) { return write(d); }

    public int writeString(String str) { return write(str); }

    @Deprecated
    public int writeTrue() { return write(true); }

    @Deprecated
    public int writeFalse() { return write(false); }

    @Deprecated
    public int writeNull() { return write(null); }

    private int write(Object value) {
        int hash = Objects.hashCode(value);
        if (root == null) {
            root = new Entry(hash, value, totalIndex++);
            return root.index;
        }
        Entry entry = root;
        while (true) {
            if (hash < entry.hash) {
                if (entry.left == null) {
                    entry.left = new Entry(hash, value, totalIndex++);
                    return entry.left.index;
                }
                entry = entry.left;
            } else {
                if (hash == entry.hash && Objects.equals(value, entry.value)) {
                    return entry.index;
                }
                if (entry.right == null) {
                    entry.right = new Entry(hash, value, totalIndex++);
                    return entry.right.index;
                }
                entry = entry.right;
            }
        }
    }

    public Object[] toArray() {
        Object[] values = new Object[totalIndex];
        if (root != null) {
            toArray(values, root);
        }
        return values;
    }

    private void toArray(Object[] values, Entry entry) {
        if (entry.left != null) {
            toArray(values, entry.left);
        }
        if (entry.right != null) {
            toArray(values, entry.right);
        }
        values[entry.index] = entry.value;
    }
}
