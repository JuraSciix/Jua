package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Astore implements Instruction {

    public static final Astore INSTANCE = new Astore();

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