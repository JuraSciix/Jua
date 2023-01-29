package jua.runtime.heap;

import jua.interpreter.Address;
import jua.interpreter.AddressUtils;

import java.util.Arrays;
import java.util.StringJoiner;

public final class ListHeap implements Heap {

    private final Address[] elements;

    public ListHeap(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
        elements = AddressUtils.allocateMemory(size, 0);
    }

    public ListHeap(Address[] source) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (source.length == 0) {
            elements = new Address[0];
        } else {
            elements = new Address[source.length];
            AddressUtils.arraycopy(source, 0, elements, 0, source.length);
        }
    }

    public int length() {
        return elements.length;
    }

    public Address get(int index) {
        return elements[index];
    }

    public void set(int index, Address value, Address oldValueReceptor) {
        if (oldValueReceptor != null) {
            oldValueReceptor.set(elements[index]);
        }
        elements[index].set(value);
    }

    public void clear() {
        for (Address element : elements) {
            element.reset();
        }
    }

    public boolean contains(Address value) {
        for (Address element : elements) {
            if (element.weakCompare(value, -1) == 0) {
                return true;
            }
        }
        return false;
    }

    public int compare(ListHeap another, int except) {
        Address[] te = this.elements;
        Address[] ae = another.elements;
        int minlen = Math.min(te.length, ae.length);
        for (int i = 0; i < minlen; i++) {
            int cmp = te[i].weakCompare(ae[i], except);
            if (cmp != 0) return cmp;
        }
        return te.length - ae.length;
    }

    public ListHeap copy() {
        return this;
    }

    public ListHeap deepCopy() {
        return new ListHeap(elements);
    }

    @Override
    public int hashCode() { return Arrays.hashCode(elements); }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListHeap h = (ListHeap) o;
        return Arrays.equals(elements, h.elements);
    }

    @Override
    public String toString() {
        StringJoiner buffer = new StringJoiner(", ", "[", "]");
        for (Address element : elements) {
            buffer.add(element.toString());
        }
        return buffer.toString();
    }
}
