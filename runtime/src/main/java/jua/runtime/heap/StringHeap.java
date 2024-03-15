package jua.runtime.heap;

import java.util.stream.IntStream;

public final class StringHeap extends Heap implements CharSequence, Comparable<StringHeap> {

    private final StringBuilder data;

    private volatile boolean hcCalculated;
    private volatile int hcValue;

    public StringHeap() {
        data = new StringBuilder(0);
        hcCalculated = true;
        hcValue = 0;
    }

    public StringHeap(CharSequence csq) {
        if (csq instanceof StringHeap) {
            StringHeap h = (StringHeap) csq;
            data = new StringBuilder(h.data);
            hcCalculated = h.hcCalculated;
            hcValue = h.hcValue;
        } else {
            data = new StringBuilder(csq);
            hcCalculated = false;
            hcValue = 0;
        }
    }

    public StringHeap(CharSequence csq, int start, int end) {
        if (csq instanceof StringHeap) {
            StringHeap h = (StringHeap) csq;
            data = new StringBuilder().append(h.data, start, end);
        } else {
            data = new StringBuilder().append(csq, start, end);
        }
        hcCalculated = false;
        hcValue = 0;
    }

    @Override
    public int length() {
        return data.length();
    }

    @Override
    public char charAt(int index) {
        return data.charAt(index);
    }

    @Override
    public StringHeap subSequence(int start, int end) {
        return new StringHeap(this, start, end);
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    public boolean nonEmpty() {
        return !isEmpty();
    }

    public int codePointAt(int index) {
        return data.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return data.codePointBefore(index);
    }

    public IntStream codePoints() {
        return data.codePoints();
    }

    public IntStream chars() {
        return data.chars();
    }

    public void setLength(int newLength) {
        resetCaches();
        data.setLength(newLength);
    }

    public StringHeap append(long value) {
        resetCaches();
        data.append(value);
        return this;
    }

    public StringHeap append(double value) {
        resetCaches();
        data.append(value);
        return this;
    }

    public StringHeap append(boolean value) {
        resetCaches();
        data.append(value);
        return this;
    }

    public StringHeap append(CharSequence value) {
        resetCaches();
        if (value instanceof StringHeap) {
            StringHeap h = (StringHeap) value;
            data.append(h.data);
        } else {
            data.append(value);
        }
        return this;
    }

    public StringHeap append(char ch) {
        resetCaches();
        data.append(ch);
        return this;
    }

    public StringHeap appendCodePoint(int codePoint) {
        resetCaches();
        data.appendCodePoint(codePoint);
        return this;
    }

    public StringHeap appendNull() {
        resetCaches();
        data.append((CharSequence) null);
        return this;
    }

    public void resetCaches() {
        hcCalculated = false;
    }

    @Override
    public StringHeap refCopy() {
        return this;
    }

    @Override
    public StringHeap deepCopy() {
        return new StringHeap(this);
    }

    @Override
    public int compareTo(StringHeap o) {
        int len1 = length();
        int len2 = o.length();
        if (len1 != len2) return len1 - len2;
        for (int i = 0; i < len1; i++) {
            char c1 = charAt(i);
            char c2 = o.charAt(i);
            if (c1 != c2) return c1 - c2;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        if (!hcCalculated) {
            int hc = 0;
            for (int i = 0; i < data.length(); i++) {
                hc = hc * 17 + Character.hashCode(data.charAt(i));
            }
            hcValue = hc;
            hcCalculated = true;
        }
        return hcValue;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StringHeap)) {
            return false;
        }
        // todo: verctorized mismatch
        return compareTo((StringHeap) o) == 0;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
