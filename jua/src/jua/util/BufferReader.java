package jua.util;

import java.io.IOException;

public final class BufferReader implements AutoCloseable {

    private char[] buf;

    private int pos;

    private final int limit;

    public BufferReader(char[] buffer, int start, int count) {
        buf = buffer;
        pos = start;
        limit = start + count;
    }

    public int position() throws IOException { return pos; }

    public int available() throws IOException { return limit - pos; }

    public boolean unreadAvailable() throws IOException { return pos < limit; }

    // todo: Рефакторинг

    public int peekChar() throws IOException {
        return peekChar(0);
    }

    public int peekChar(int offset) throws IOException {
        return available() > offset ? buf[pos + offset] : -1;
    }

    public int readChar() throws IOException {
        int c = peekChar();
        if (c >= 0) pos++;
        return c;
    }

    public int peekCodePoint() throws IOException {
        return peekCodePoint(0);
    }

    public int peekCodePoint(int offset) throws IOException {
        return 0; // todo
    }

    public int readCodePoint() throws IOException {
        int cp = peekCodePoint();
        if (cp > 0) pos += Character.charCount(cp);
        return cp;
    }

    @Override
    public void close() throws IOException {
        ensureUnclosed();
        buf = null;
    }

    private void ensureUnclosed() throws IOException {
        if (buf == null) {
            throw new IOException("Reader is closed");
        }
    }
}
