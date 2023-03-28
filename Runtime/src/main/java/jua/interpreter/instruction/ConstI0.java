package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class ConstI0 implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("const_i0");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.constInt(0L);
    }
}
