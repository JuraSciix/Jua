package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Astore implements Instruction {

    @Override
    public int stackAdjustment() { return -1 + -1 + -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("astore");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackAstore();
    }
}