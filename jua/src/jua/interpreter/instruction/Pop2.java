package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Pop2 implements Instruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop2");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.pop2();
    }
}