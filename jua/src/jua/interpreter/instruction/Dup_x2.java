package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Dup_x2 implements Instruction {

    public static final Dup_x2 INSTANCE = new Dup_x2();

    @Override
    public int stackAdjustment() {
        return 1;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup_x2");
    }

    @Override
    public void run(InterpreterState state) {
        state.dup1_x2();
    }
}