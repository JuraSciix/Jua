package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Return implements Instruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("return");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.impl_return();
    }
}