package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Dup_x1 implements Instruction {

    public static final Dup_x1 INSTANCE = new Dup_x1();

    @Override
    public int stackAdjustment() {
        return 1;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup_x1");
    }

    @Override
    public void run(InterpreterState state) {
        state.dup1_x1();
    }
}