package jua.parser.ast;

// todo: У меня уже имеется механизм для координации по файлу по линиям и столбцам.
// todo: внедрить этот механизм в эту версию
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
