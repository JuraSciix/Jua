package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Dup implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.dup();
    }
}