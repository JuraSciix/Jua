package jua.compiler;

import jua.parser.tree.Position;

public class CompileError extends RuntimeException {

    public final Position position;

    public CompileError(String message, Position position) {
        super(message);
        this.position = position;
    }
}
