package jua.interpreter.instructions;

import jua.interpreter.InterpreterState;

public final class Iftrue extends ChainInstruction {

    public Iftrue(int destIp) {
        super(destIp);
    }

    @Override
    public int run(InterpreterState state) {
        return state.popStack().booleanValue() ? destIp : NEXT;
    }
}
