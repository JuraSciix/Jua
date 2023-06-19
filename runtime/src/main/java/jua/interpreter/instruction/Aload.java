package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Aload implements Instruction {

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("aload");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackAload();
    }
}