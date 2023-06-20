package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Leave implements Instruction {

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("leave");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.impl_leave();
    }
}