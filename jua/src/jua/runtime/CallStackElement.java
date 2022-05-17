package jua.runtime;

import jua.interpreter.InterpreterFrame;

@Deprecated
public class CallStackElement {

    public final String name;

    public final String filename;

    public final int line;

    public final Operand[] args;

    public final InterpreterFrame lastFrame;

    public CallStackElement(String name, String filename, int line, Operand[] args, InterpreterFrame lastFrame) {
        this.name = name;
        this.filename = filename;
        this.line = line;
        this.args = args;
        this.lastFrame = lastFrame;
    }
}
