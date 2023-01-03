package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Dup2_x2 implements Instruction {

    @Override
    public int stackAdjustment() {
        return 1 + 1;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup_x2");
    }

    @Override
    public void run(InterpreterState state) {
        state.dup2_x2();
    }
}