package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Adrop implements Instruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("adrop");
    }

    @Override
    public boolean run(InterpreterState state) {
        throw new AssertionError(this); // todo
    }
}