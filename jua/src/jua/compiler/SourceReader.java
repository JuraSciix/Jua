package jua.compiler;

public final class SourceReader {

    public final char[] buffer;

    public final int limit;

    public int cursor;

    public int cached = -1;

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

    public int pos() {   return cursor; }

    public int peek() {
        if (cached < 0 && cursor < limit) {
            cached = Character.codePointAt(buffer, cursor);
        }
        return cached;
    }

    public boolean next() {
        if (cached >= 0) {
            cursor += Character.charCount(cached);
            cached = -1;
        } else if (cursor < limit) {
            cursor += Character.charCount(Character.codePointAt(buffer, cursor));
        }
        return cursor < limit;
    }

}
