package jua.interpreter.runtime;

import jua.interpreter.Program;
import jua.interpreter.ProgramFrame;

@De
public class CallStackElement {

    public static CallStackElement mainEntry() {
        return new CallStackElement(null, null, -1, null,
                Program.createMain().makeFrame());
    }

    public final String name;

    public final String filename;

    public final int line;

    public final Operand[] args;

    public final ProgramFrame lastFrame;

    public CallStackElement(String name, String filename, int line, Operand[] args, ProgramFrame lastFrame) {
        this.name = name;
        this.filename = filename;
        this.line = line;
        this.args = args;
        this.lastFrame = lastFrame;
    }
}
