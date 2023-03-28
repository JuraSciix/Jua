package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

// todo: rename to Getsize (length -> getsize)
public final class Length implements Instruction {

    @Override
    public int stackAdjustment() { return -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("length");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackLength();
    }
}
