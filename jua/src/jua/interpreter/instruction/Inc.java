package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Inc implements Instruction {

    private final int index;

    public Inc(int index) {
        this.index = index;
    }

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("inc");
        printer.printLocal(index);
    }

    @Override
    public void run(InterpreterState state) {
        state.stackInc(index);
    }
}