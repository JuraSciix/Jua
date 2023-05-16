package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class Leave implements Instruction {

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("leave");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.impl_leave();
    }
}