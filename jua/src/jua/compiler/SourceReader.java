package jua.compiler;

import java.util.Objects;

public final class SourceReader implements AutoCloseable {

    private static final int UNPEEKED = -1;

    public static SourceReader of(String str) {
        return wrap(str.toCharArray());
    }

    public static SourceReader wrap(char[] buffer) {
        Objects.requireNonNull(buffer, "buffer");
        return new SourceReader(buffer, 0, buffer.length);
    }

    public static SourceReader wrap(char[] buffer, int offset, int length) {
        Objects.requireNonNull(buffer, "buffer");
        if (offset < 0 || length < 0 || (offset + length) >= buffer.length) {
            throw new IndexOutOfBoundsException("buffer.length: " + buffer.length +
                    "; offset: " + offset +
                    "; length: " + length);
        }
        return new SourceReader(buffer, offset, length);
    }

    private final char[] buffer;

    private final int length;

    private int cursor;

    private int peeked = UNPEEKED;

    private SourceReader(char[] buffer, int offset, int length) {
        this.buffer = buffer;
        this.length = offset + length;
        cursor = offset;
    }

    public boolean hasMore() { return cursor() < length(); }
    public int cursor() { return cursor; }
    public int length() { return length; }

    public char peekChar() {
        if (peeked == -1) {
            peeked = buffer[cursor];
        }
        if (Character.charCount(peeked) > 1)
            return Character.highSurrogate(peeked);
        else
            return (char) peeked;
    }

    public char readChar() {
        int c;
        if (peeked != -1) {
            if (Character.charCount(peeked) > 1) {
                c = Character.highSurrogate(peeked);
                peeked = Character.lowSurrogate(peeked);
            } else {
                c = peeked;
                peeked = -1;
            }
        } else {
            c = buffer[cursor];
        }
        cursor++;
        return (char) c;
    }

    public int peekCodePoint() {
        if (peeked == -1) {
            peeked = readCodePointInternal();
        }
        return peeked;
    }

    public int readCodePoint() {
        int c;
        if (peeked != -1) {
            c = peeked;
            peeked = -1;
        } else {
            c = readCodePointInternal();
        }
        cursor += Character.charCount(c);
        return c;
    }

    private int readCodePointInternal() {
        int left = length() - cursor();

        if (left < 1) {
            throw new IndexOutOfBoundsException();
        }

        char high = buffer[cursor];

        if (left >= 2) {
            char low = buffer[cursor + 1];

            if (Character.isSurrogatePair(high, low)) {
                return Character.toCodePoint(high, low);
            }
        }

        return high;
    }

    @Override
    public void close() { /* no-op */ }
}
