package jua.interpreter;

public class RuntimeError extends RuntimeException {

    public final String filename;

    public final int line;

    // throws by only Program
    RuntimeError(String message, String filename, int line) {
        super(message);
        this.filename = filename;
        this.line = line;
    }
}
