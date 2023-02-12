package jua.compiler;

public final class Source {

    public final String fileName;

    public final char[] content;

    private LineMap _linemap;

    public Source(String fileName, char[] content) {
        this.fileName = fileName;
        this.content = content;
    }

    public LineMap getLineMap() {
        if (_linemap == null) {
            _linemap = new LineMap(content);
        }
        return _linemap;
    }
}
