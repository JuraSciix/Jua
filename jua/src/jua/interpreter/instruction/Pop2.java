package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Pop2 implements Instruction {

    public static final Pop2 INSTANCE = new Pop2();

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop2");
    }

    @Override
    public void run(InterpreterState state) {
        state.popStack();
        state.popStack();
    }
}