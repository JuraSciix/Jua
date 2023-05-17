package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Dec implements Instruction {

    private final int id;

    public Dec(int id) {
        this.id = id;
    }

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dec");
        printer.printLocal(id);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVDec(id);
    }
}