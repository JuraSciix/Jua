package jua.compiler;

public final class SourceReader {

    public final char[] buffer;

    public final int limit;

    public int cursor;

    public int peeked = -1;

    public SourceReader(char[] buffer, int start, int limit) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer must not be null");
        }
        if (limit > buffer.length) {
            throw new IllegalArgumentException("limit exceeds the length of buffer: " + limit + ", " + buffer.length);
        }
        if (start > limit) {
            throw new IllegalArgumentException("start exceeds the limit: " + start + ", " + limit);
        }
        if (start < 0) {
            throw new IllegalArgumentException("start is negative: " + start);
        }
        this.buffer = buffer;
        this.cursor = start;
        this.limit = limit;
    }

    public int peek() {
        if (peeked == -1) {
            peeked = read();
        }
        return peeked;
    }

    public boolean next() {
        int c = read();
        if (c >= 0) {
            cursor += Character.charCount(c);
        }
        return cursor < limit;
    }

    private int read() {
        if (peeked != -1) {
            int p = peeked;
            peeked = -1;
            return p;
        }
        if (cursor >= limit) {
            return -1;
        }
        char high = buffer[cursor];
        if (Character.isHighSurrogate(high)) {
            if ((cursor + 1) < limit) {
                char low = buffer[cursor + 1];
                if (Character.isLowSurrogate(low)) {
                    return Character.toCodePoint(high, low);
                }
            }
        }
        return high;
    }
}
