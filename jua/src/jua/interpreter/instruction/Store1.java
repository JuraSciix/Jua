package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Store1 implements Instruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("store_1");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVStore(1);
    }
}