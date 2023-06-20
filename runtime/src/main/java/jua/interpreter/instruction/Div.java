package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Div implements Instruction {

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("div");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackDiv();
    }
}