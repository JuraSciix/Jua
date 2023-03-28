package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class Shr implements Instruction {

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("shr");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackShr();
    }
}