package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Dup_x1 implements Instruction {

    @Override
    public int stackAdjustment() {
        return 1;
    }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("dup_x1");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.dup1_x1();
    }
}