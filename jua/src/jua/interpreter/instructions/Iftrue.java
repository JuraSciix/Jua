package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;

public final class Iftrue extends ChainInstruction {

    public Iftrue(int destIp) {
        super(destIp);
    }

    @Override
    public int run(InterpreterThread thread) {
        return thread.popStack().booleanValue() ? destIp : NEXT;
    }
}
