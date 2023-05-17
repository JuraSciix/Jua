package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Dup2_x1 implements Instruction {

    @Override
    public int stackAdjustment() {
        return 1 + 1;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2_x1");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.dup2_x1();
    }
}