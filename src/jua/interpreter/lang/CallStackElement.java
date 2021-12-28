package jua.interpreter.lang;

import jua.interpreter.Program;
import jua.interpreter.Frame;

public class CallStackElement {

    public static CallStackElement mainEntry() {
        return new CallStackElement(null, null, -1, null,
                Program.createMain().makeFrame());
    }

    public final String name;

    public final String filename;

    public final int line;

    public final Operand[] args;

    public final Frame lastFrame;

    public CallStackElement(String name, String filename, int line, Operand[] args, Frame lastFrame) {
        this.name = name;
        this.filename = filename;
        this.line = line;
        this.args = args;
        this.lastFrame = lastFrame;
    }
}
