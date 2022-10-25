package jua.compiler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Source {

    private final Path path;

    private char[] content;

    private LineMap lineMap;

    public Source(String path) {
        this.path = Paths.get(path);
    }

    public Log createLog() {
        return new Log(); // todo
    }

    public Target target() {
        return new Target(Version.JUA_1_3); // todo
    }

    public String filename() {
        return path.toString();
    }

    public char[] buffer() {
        return content.clone();
    }

    public void read() throws IOException {
        byte[] fileContentBytes = Files.readAllBytes(path);
        ByteBuffer fileContentByteBuffer = ByteBuffer.wrap(fileContentBytes);
        content = StandardCharsets.UTF_8.decode(fileContentByteBuffer).array();
    }

    public SourceReader createReader() {
        ensureContent();
        return SourceReader.wrap(content);
    }

    private void ensureContent() {
        if (content == null) {
            throw new IllegalStateException("Content was not loaded. Call read() before it");
        }
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
