package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Pop implements Instruction {

    public static final Pop INSTANCE = new Pop();

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop");
    }

    @Override
    public void run(InterpreterState state) {
        state.pop();
    }
}