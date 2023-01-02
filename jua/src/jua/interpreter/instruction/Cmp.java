package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

import java.io.PrintWriter;

public final class Cmp implements Instruction {

    public static final Cmp INSTANCE = new Cmp();

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("add");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackCmp();
    }
}