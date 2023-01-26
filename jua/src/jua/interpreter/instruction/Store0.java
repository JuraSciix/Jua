package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Store0 implements Instruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("store_0");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackVStore(0);
    }
}