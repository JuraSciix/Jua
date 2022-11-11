package jua.compiler;

@Deprecated
public class CompileError extends RuntimeException {

    public final int position;

    public CompileError(String message, int position) {
        super(message);
        this.position = position;
    }
}
