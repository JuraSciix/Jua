package jua.interpreter.lang;

import jua.interpreter.Program;

public class CallStackElement {

    public static CallStackElement entry() {
        return new CallStackElement(null, null, -1, null, Program.Builder.empty().build());
    }

    public final String name;

    public final String filename;

    public final int line;

    public final Operand[] args;

    public final Program lastProgram;

    public CallStackElement(String name, String filename, int line, Operand[] args, Program lastProgram) {
        this.name = name;
        this.filename = filename;
        this.line = line;
        this.args = args;
        this.lastProgram = lastProgram;
    }
}
