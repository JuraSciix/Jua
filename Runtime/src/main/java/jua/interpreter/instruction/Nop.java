package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Nop implements Instruction {

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("nop");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.nop();
    }
}
