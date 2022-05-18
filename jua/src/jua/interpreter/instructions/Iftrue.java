package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;

public final class Iftrue extends ChainInstruction {

    public Iftrue(int destIp) {
        super(destIp);
    }

    @Override
    public int run(InterpreterThread env) {
        return env.popStack().booleanValue() ? destIp : NEXT;
    }
}
