package jua.compiler;

public final class Source {

    public final String name, content;

    private LineMap _linemap;

    private Log _log;

    public Source(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public LineMap getLineMap() {
        if (_linemap == null)
            _linemap = new LineMap(content);
        return _linemap;
    }

    public Log getLog() {
        if (_log == null)
            _log = new Log(this);
        return _log;
    }
}
