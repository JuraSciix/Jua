package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Pop implements Instruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.pop();
    }
}