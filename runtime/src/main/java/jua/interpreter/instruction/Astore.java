package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Astore implements Instruction {

    @Override
    public int stackAdjustment() { return -1 + -1 + -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("astore");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackAstore();
    }
}