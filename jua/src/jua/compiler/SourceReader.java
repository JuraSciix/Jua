package jua.compiler;

import java.util.Objects;

public final class SourceReader implements AutoCloseable {

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

    private int peekedChar = -1;

    private int peekedCodePoint = -1;

    private SourceReader(char[] buffer, int offset, int length) {
        this.buffer = buffer;
        this.length = offset + length;
        cursor = offset;
    }

    public boolean hasMore() { return cursor() < length(); }

    public int cursor() { return cursor; }

    public int length() { return length; }

    public char peekChar() {
        if (peekedChar == -1) {
            peekedChar = readCharInternal();
        }

        return (char) peekedChar;
    }

    public char readChar() {
        if (peekedChar != -1) {
            int ch = peekedChar;
            peekedChar = -1;
            return (char) ch;
        }

        return readCharInternal();
    }

    private char readCharInternal() {
        if (!hasMore()) {
            throw new IndexOutOfBoundsException();
        }

        return buffer[cursor++];
    }

    public int peekCodePoint() {
        if (peekedCodePoint == -1) {
            peekedCodePoint = readCodePointInternal();
        }

        return peekedCodePoint;
    }

    public int readCodePoint() {
        if (peekedCodePoint != -1) {
            int codePoint = peekedCodePoint;
            peekedCodePoint = -1;
            return codePoint;
        }

        return readCodePointInternal();
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
                cursor += 2;
                return Character.toCodePoint(high, low);
            }
        }

        cursor += 1;

        return high;
    }

    @Override
    public void close() throws Exception {
        /* no-op */
    }
}
