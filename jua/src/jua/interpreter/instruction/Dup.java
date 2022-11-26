package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Dup implements Instruction {

    public static final Dup INSTANCE = new Dup();

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup");
    }

    @Override
    public void run(InterpreterState state) {
        state.dup();
    }
}