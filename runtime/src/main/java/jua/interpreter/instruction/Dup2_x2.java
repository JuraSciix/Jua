package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Dup2_x2 implements Instruction {

    @Override
    public int stackAdjustment() {
        return 1 + 1;
    }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("dup_x2");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.dup2_x2();
    }
}