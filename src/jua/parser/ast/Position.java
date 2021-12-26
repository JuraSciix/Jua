package jua.parser.ast;

public class Position {

    public final String filename;

    public final int line;

    public final int offset;

    public Position(String filename, int line, int offset) {
        this.filename = filename;
        this.line = line;
        this.offset = offset;
    }
}
