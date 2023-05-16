package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Inc implements Instruction {

    private final int id;

    public Inc(int id) { this.id = id; }

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("inc");
        printer.printLocal(id);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVInc(id);
    }
}