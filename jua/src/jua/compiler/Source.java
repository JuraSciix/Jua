package jua.compiler;

public final class Source {

    public final String fileName;

    public final char[] content;

    public final Log log;

    private LineMap _linemap;


    public Source(String fileName, char[] content, Log log) {
        this.fileName = fileName;
        this.content = content;
        this.log = log;
    }

    public LineMap getLineMap() {
        if (_linemap == null)
            _linemap = new LineMap(content);
        return _linemap;
    }

}
