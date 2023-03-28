package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class Newmap implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("newmap");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stack_newmap();
    }
}