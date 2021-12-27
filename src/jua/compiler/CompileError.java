package jua.compiler;

import jua.parser.Tree;

public class CompileError extends RuntimeException {

    public final Tree.Position position;

    public CompileError(String message, Tree.Position position) {
        super(message);
        this.position = position;
    }
}
