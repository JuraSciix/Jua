package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class ConstI1 implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("const_i1");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.constInt(1L);
    }
}
