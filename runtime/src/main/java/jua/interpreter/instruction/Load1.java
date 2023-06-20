package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Load1 implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("load_1");
        printer.printLocal(1);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVLoad(1);
    }
}