package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class Load2 implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("load_2");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVLoad(2);
    }
}