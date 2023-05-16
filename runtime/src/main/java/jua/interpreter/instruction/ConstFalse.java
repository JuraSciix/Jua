package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class ConstFalse implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("const_false");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.constFalse();
    }
}
