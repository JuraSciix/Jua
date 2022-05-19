package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;

public final class Iftrue extends ChainInstruction {

    public Iftrue(int destIp) {
        super(destIp);
    }

    @Override
    public int run(InterpreterRuntime env) {
        return env.popStack().booleanValue() ? destIp : NEXT;
    }
}
