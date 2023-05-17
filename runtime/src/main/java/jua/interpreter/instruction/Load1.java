package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

// todo: rename to Load (vload -> load)
public class Load1 implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("load_1");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackVLoad(1);
    }
}