package jua.runtime.heap;

import java.io.IOException;
import java.io.Writer;

public final class StringHeap implements CharSequence, Comparable<StringHeap>, Heap {

    private final StringBuilder buffer;

    // Кеш-содержащие поля
    private volatile transient int hashCode;
    private volatile transient boolean hashCodeCalculated = false;

    public StringHeap() {
        this("", 0, 0);
    }

    public StringHeap(String source) {
        this(source, 0, source.length());
    }

    public StringHeap(String source, int offset, int count) {
        if (count == 0) {
            buffer = new StringBuilder(1);
            hashCodeCalculated = true; // Хеш-код пустой строки всегда равняется нулю.
        } else {
            buffer = new StringBuilder(count);
            buffer.append(source, offset, offset + count);
        }
    }

    public StringHeap(StringHeap original) {
        this(original, 0, original.length());
    }

    public StringHeap(StringHeap original, int offset, int count) {
        if (count == 0) {
            buffer = new StringBuilder(1);
            hashCodeCalculated = true;
        } else {
            buffer = new StringBuilder(count);
            buffer.append(original.buffer, offset, offset + count);
            if (offset == 0 && count == original.length()) {
                hashCode = original.hashCode;
                hashCodeCalculated = original.hashCodeCalculated;
            }
        }
    }

    @Override
    public int length() {
        return buffer.length();
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    public boolean nonEmpty() {
        return !isEmpty();
    }

    @Override
    public char charAt(int index) {
        return buffer.charAt(index);
    }

    @Override
    public StringHeap subSequence(int start, int end) {
        return new StringHeap(this, start, end - start);
    }

    public int size() {
        return buffer.length();
    }

    public boolean isSame(StringHeap that) {
        StringBuilder b1 = buffer;
        StringBuilder b2 = that.buffer;
        if (b1.length() != b2.length()) return false;
        for (int i = 0; i < b1.length(); i++)
            if (b1.charAt(i) != b2.charAt(i)) return false;
        return true;
    }

    public StringHeap copy() {
        return new StringHeap(this);
    }

    public StringHeap deepCopy() {
        return new StringHeap(this);
    }

    public StringHeap append(long l) {
        buffer.append(l);
        resetCaches();
        return this;
    }

    public StringHeap append(double d) {
        buffer.append(d);
        resetCaches();
        return this;
    }

    public StringHeap append(boolean b) {
        buffer.append(b);
        resetCaches();
        return this;
    }

    public StringHeap append(StringHeap s) {
        buffer.append(s.buffer);
        resetCaches();
        return this;
    }

    public StringHeap appendNull() {
        buffer.append("null");
        resetCaches();
        return this;
    }

    private void resetCaches() {
        hashCodeCalculated = false;
    }

    public void writeTo(Writer writer) throws IOException {
        int i = 0;
        int j = buffer.length();

        while (i < j) {
            int codePoint = buffer.codePointAt(i);
            writer.write(codePoint);
            i += Character.charCount(i);
        }
    }

    @Override
    public int compareTo(StringHeap o) {
        if (this == o) return 0;
        StringBuilder bt = buffer;
        StringBuilder bo = o.buffer;
        int lbt = bt.length();
        int lbo = bo.length();
        int lm = Math.min(lbt, lbo);
        for (int i = 0; i < lm; i++) {
            char ct = bt.charAt(i);
            char co = bo.charAt(i);
            if (ct != co) return ct - co;
        }
        return lbt - lbo;
    }

    @Override
    public int hashCode() {
        int h = hashCode;
        if (!hashCodeCalculated) {
            assert h == 0;
            for (int i = 0, l = buffer.length(); i < l; i++)
                h = (h << 5) - h + buffer.charAt(i);
            hashCode = h;
            hashCodeCalculated = true;
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return isSame((StringHeap) o);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
