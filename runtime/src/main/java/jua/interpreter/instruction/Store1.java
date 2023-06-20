package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Store1 implements Instruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("store_1");
        printer.printLocal(1);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVStore(1);
    }
}