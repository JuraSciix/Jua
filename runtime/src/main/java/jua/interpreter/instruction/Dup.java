package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Dup implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("dup");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.dup();
    }
}