package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Dec implements Instruction {

    private final int index;

    public Dec(int index) {
        this.index = index;
    }

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dec");
        printer.printLocal(index);
    }

    @Override
    public void run(InterpreterState state) {
        state.stackDec(index);
    }
}