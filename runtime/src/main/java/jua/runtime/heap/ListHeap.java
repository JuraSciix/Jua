package jua.runtime.heap;

import jua.runtime.interpreter.Address;
import jua.runtime.interpreter.AddressSupport;
import jua.runtime.interpreter.AddressUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.StringJoiner;

public final class ListHeap extends Heap implements Iterable<Address> {

    private final Address[] data;

    /** Указатель на конец списка */
    private int key = 0;

    public ListHeap(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
        data = AddressUtils.allocateMemoryNulls(size, 0);
    }

    public ListHeap(Address[] source) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        data = new Address[source.length];
        AddressUtils.arraycopy(source, 0, data, 0, source.length);
    }

    public int length() {
        return data.length;
    }

    public int key() {
        return key;
    }

    public void setKey(int key) {
        if (key < 0 || key >= length()) {
            throw new IndexOutOfBoundsException();
        }
        this.key = key;
    }

    public Address get(int index) {
        return data[index];
    }

    public Address get() {
        return get(key());
    }

    public Address add() {
        int k = key();
        setKey(k + 1);
        return data[k];
    }

    public void set(int index, Address value, Address oldValueReceptor) {
        if (oldValueReceptor != null) {
            oldValueReceptor.set(data[index]);
        }
        data[index].set(value);
    }

    public void clear() {
        if (data.length > 0) {
            data[0].setNull();
            AddressUtils.fill(data, 1, data.length, data[0]);
        }
    }

    public boolean contains(Address value) {
        for (Address e : data) {
            if (e != null) {
                if (e.fastCompareWith(value, -1) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPresentAt(int index) {
        return index >= 0 && index < data.length
                && data[index] != null
                && !data[index].isNull();
    }

    public int fastCompare(ListHeap that, int unexpected) {
        if (length() != that.length())
            return length() - that.length();
        return compare(that, unexpected);
    }

    public int compare(ListHeap another, int except) {
        Address[] te = this.data;
        Address[] ae = another.data;
        int minlen = Math.min(te.length, ae.length);
        for (int i = 0; i < minlen; i++) {
            if (te[i] == null || ae[i] == null) continue;
            int cmp = te[i].fastCompareWith(ae[i], except);
            if (cmp != 0) return cmp;
        }
        return te.length - ae.length;
    }

    public Address[] getArray() {
        return data.clone();
    }

    /** Возвращает {@code true}, если список пуст, в противном случае {@code false}. */
    public boolean isEmpty() { return data.length == 0; }

    /** Возвращает {@code false}, если список пуст, в противном случае {@code true}. */
    public boolean nonEmpty() { return !isEmpty(); }

    @Override
    public ListHeap refCopy() {
        return this;
    }

    @Override
    public ListHeap deepCopy() {
        int size = data.length;
        ListHeap copy = new ListHeap(size);
        for (int i = 0; i < size; i++) {
            Address src = data[i];
            if (src != null) {
                Address dst = copy.get(i);
                src.clone(dst);
            }
        }
        return copy;
    }

    @Override
    public Iterator<Address> iterator() {
        return Arrays.asList(data).iterator();
    }

    @Override
    public int hashCode() { return Arrays.hashCode(data); }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListHeap x = (ListHeap) o;
        return Arrays.equals(data, x.data);
    }

    @Override
    public String toString() {
        StringJoiner buffer = new StringJoiner(", ", "[", "]");
        for (Address element : data) {
            buffer.add(element.toString());
        }
        return buffer.toString();
    }
}
