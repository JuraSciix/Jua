package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Dup2 implements Instruction {

    @Override
    public int stackAdjustment() {
        return -1 + -1 + 1 + 1 + 1 + 1;
    }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("dup2");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.dup2();
    }
}