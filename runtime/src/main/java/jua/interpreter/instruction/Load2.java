package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Load2 implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("load_2");
        printer.printLocal(2);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVLoad(2);
    }
}