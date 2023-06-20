package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Load implements Instruction {

    private final int id;

    public Load(int id) {
        this.id = id;
    }

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("load");
        printer.printLocal(id);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVLoad(id);
    }
}