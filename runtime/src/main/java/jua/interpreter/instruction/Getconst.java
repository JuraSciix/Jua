package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Getconst implements Instruction {

    private final int id;

    public Getconst(int id) {
        this.id = id;
    }

    @Override
    public int stackAdjustment() {
        return 1;
    }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("getconst");
        printer.printConstRef(id);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.getconst(id);
    }
}