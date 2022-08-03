package jua.util;

import java.io.IOException;

public final class Source {

    private final String filename;

    private final char[] buffer;

    private LineMap lineMap;

    public Source(String filename, char[] buffer) {
        this.filename = filename;
        this.buffer = buffer;
    }

    public String filename() { return filename; }
    public char[] buffer() { return buffer.clone(); }

    public BufferReader createReader() {
        return new BufferReader(buffer, 0, buffer.length);
    }

    public LineMap getLineMap() throws IOException {
        ensureLineMapInitiated();
        return lineMap;
    }

    private void ensureLineMapInitiated() throws IOException {
        if (lineMap == null) {
            lineMap = new LineMap(this);
        }
    }
}
