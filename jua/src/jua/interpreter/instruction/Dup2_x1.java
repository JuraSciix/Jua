package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Dup2_x1 implements Instruction {

    public static final Dup2_x1 INSTANCE = new Dup2_x1();

    @Override
    public int stackAdjustment() {
        return 1 + 1;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2_x1");
    }

    @Override
    public int run(InterpreterState state) {
        state.dup2_x1();
        return NEXT;
    }
}