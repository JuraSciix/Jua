package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class Rem implements Instruction {

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("rem");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackRem();
    }
}