package jua.compiler;

import java.util.Objects;

public final class Source {

    public final String fileName;

    public final char[] content;

    private LineMap _linemap;

    private final Log log;

    public Source(String fileName, char[] content, Log log) {
        this.fileName = fileName;
        this.content = content;
        this.log = Objects.requireNonNull(log);
    }

    public SourceReader getReader() {
        return new SourceReader(content, 0, content.length);
    }

    public Log getLog() {
        return log;
    }

    public LineMap getLineMap() {
        if (_linemap == null) {
            _linemap = new LineMap(content);
        }
        return _linemap;
    }
}
