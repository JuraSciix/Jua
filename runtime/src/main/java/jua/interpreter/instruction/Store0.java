package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Store0 implements Instruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("store_0");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVStore(0);
    }
}