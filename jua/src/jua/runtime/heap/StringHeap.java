package jua.runtime.heap;

import java.io.IOException;
import java.io.Writer;

public final class StringHeap implements CharSequence, Heap, Comparable<StringHeap> {

    private static final ThreadLocal<StringHeap> TMP = new ThreadLocal<StringHeap>() {
        @Override
        protected StringHeap initialValue() {
            return new StringHeap();
        }
    };

    public static StringHeap temp() {
        return TMP.get();
    }

    private final StringBuffer buffer;

    // Кеш-содержащие поля
    private volatile transient int hashCode;
    private volatile transient boolean hashCodeCalculated = false;

    public StringHeap() {
        buffer = new StringBuffer(0);
        hashCodeCalculated = true; // Хеш-код пустой строки всегда равняется нулю.
    }

    public StringHeap(String initialString) {
        buffer = new StringBuffer(initialString.length());
        buffer.append(initialString);
    }

    public StringHeap(String initialString, int offset, int count) {
        buffer = new StringBuffer(count);
        buffer.append(initialString, offset, offset + count);
    }

    public StringHeap(StringHeap original) {
        buffer = new StringBuffer(original.buffer);
        hashCode = original.hashCode;
        hashCodeCalculated = original.hashCodeCalculated;
    }

    public StringHeap(StringHeap original, int offset, int count) {
        buffer = new StringBuffer(count);
        buffer.append(original.buffer, offset, offset + count);
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    public boolean nonEmpty() {
        return !isEmpty();
    }

    @Override
    public int length() { return buffer.length(); }

    @Override
    public char charAt(int index) { return buffer.charAt(index); }

    @Override
    public CharSequence subSequence(int start, int end) { return new StringHeap(this, start, end - start); }

    public int size() { return buffer.length(); }

    public boolean isSame(StringHeap that) {
        StringBuffer b1 = buffer;
        StringBuffer b2 = that.buffer;
        if (b1.length() != b2.length()) return false;
        for (int i = 0; i < b1.length(); i++)
            if (b1.charAt(i) != b2.charAt(i)) return false;
        return true;
    }

    public int compare(StringHeap that) {
        StringBuffer b1 = buffer;
        StringBuffer b2 = that.buffer;
        for (int i = 0, j = Math.min(b1.length(), b2.length()); i < j; i++) {
            char ch1 = b1.charAt(i);
            char ch2 = b2.charAt(i);
            if (ch1 != ch2) {
                return Integer.compare(ch1, ch2);
            }
        }
        return Integer.compare(b1.length(), b2.length());
    }

    public StringHeap copy() { return new StringHeap(this); }

    public StringHeap deepCopy() { return new StringHeap(this); }

    public StringHeap append(long j) {
        buffer.append(j);
        resetCaches();
        return this;
    }

    public StringHeap append(boolean z) {
        resetCaches();
        buffer.append(z);
        return this;
    }

    public StringHeap append(double d) {
        buffer.append(d);
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
        // todo: Исправить ленивую реализацию
        return buffer.toString().compareTo(o.buffer.toString());
    }

    @Override
    public int hashCode() {
        if (hashCodeCalculated) return hashCode;
        StringBuffer b = buffer;
        int h = 0;
        for (int i = 0; i < b.length(); i++)
            h = (h << 5) - h + b.charAt(i);
        hashCode = h;
        hashCodeCalculated = true;
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
        return '"' + buffer.toString() + '"';
    }
}
