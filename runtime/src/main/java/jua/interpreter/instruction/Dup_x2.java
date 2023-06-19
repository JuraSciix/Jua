package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Dup_x2 implements Instruction {

    @Override
    public int stackAdjustment() {
        return 1;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup_x2");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.dup1_x2();
    }
}