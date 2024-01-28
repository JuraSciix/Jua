package jua.compiler;

import java.io.PrintStream;
import java.io.PrintWriter;

public class SimpleLog extends Log {

    /** Поток вывода ошибок. */
    private final PrintWriter stderr;

    /** Максимальное число выводимых ошибок. */
    private final int errorLimit;

    public SimpleLog(PrintStream stderr, int errorLimit) {
        this(new PrintWriter(stderr), errorLimit);
    }

    public SimpleLog(PrintWriter stderr, int errorLimit) {
        if (stderr == null) {
            throw new IllegalArgumentException("stderr must not be null");
        }
        if (errorLimit <= 0) {
            throw new IllegalArgumentException("error limit must be greater than zero");
        }
        this.stderr = stderr;
        this.errorLimit = errorLimit;
    }

    public void error(Source source, int pos, String message) {
        doError(source, pos, message);
    }

    protected void doError(Source source, int pos, String msg) {
        if (++errorCounter > errorLimit) return;

        // ========= Формат =========
        // Compile error: {message}
        // Location: in {file} at line {line}.
        // {source-line}
        // {column-pointer}
        // ========= Пример =========
        // Compile error: variable redeclaration.
        // Location: in script.jua at line 1.
        // var a;
        //     ^
        // ==========================

        LineMap lineMap = source.getLineMap();
        int lineNum     = lineMap.getLineNumber(pos);
        int colNum      = lineMap.getColumnNumber(pos);
        int lineStart   = lineMap.getLineStart(lineNum);
        int lineEnd     = lineMap.getLineEnd(lineNum);

        stderr.println("Compile error: " + msg);
        stderr.println("Location: in " + source.fileName + " at line " + lineNum + ".");
        stderr.write(source.content, lineStart, lineEnd - lineStart);
        stderr.println();
        for (int i = 0; i < colNum && i < source.content.length; i++) {
            stderr.print(source.content[lineStart + i] == '\t' ? '\t' : ' ');
        }
        stderr.println('^');

        stderr.flush();
    }
}
