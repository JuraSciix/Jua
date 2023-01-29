package jua.compiler;

import jua.utils.StringUtils;

import java.io.PrintStream;

public class Log {

    /** Поток вывода ошибок. */
    private final PrintStream stderr;

    /** Максимальное число выводимых ошибок. */
    private final int errorLimit;

    /** Счетчик ошибок. */
    private int errorCounter = 0;

    public Log(PrintStream stderr, int errorLimit) {
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
        validateParameters(source, pos, message);
        doError(source, pos, message);
    }

    public void error(Source source, int pos, String message, Object... args) {
        validateParameters(source, pos, message);
        doError(source, pos, String.format(message, args));
    }

    private static void validateParameters(Source source, int pos, String message) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (pos < 0) {
            throw new IllegalArgumentException("pos must not be negative");
        }
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException("message must not be blank");
        }
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
        int lineNum = lineMap.getLineNumber(pos);
        int colNum = lineMap.getColumnNumber(pos);
        String lineSrc = source.content.substring(lineMap.getLineStart(lineNum), lineMap.getLineEnd(lineNum));

        stderr.println("Compile error: " + msg);
        stderr.println("Location: in " + source.fileName + " at line " + lineNum + ".");
        stderr.println(lineSrc);
        for (int i = 0; i < colNum && i < lineSrc.length(); i++) {
            stderr.print(lineSrc.charAt(i) == '\t' ? '\t' : ' ');
        }
        stderr.println('^');
    }

    public boolean hasErrors() { return errorCounter > 0; }
}
