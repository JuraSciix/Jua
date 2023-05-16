package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class Store implements Instruction {

    private final int id;

    public Store(int id) {
        this.id = id;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("store");
        printer.printLocal(id);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVStore(id);
    }
}