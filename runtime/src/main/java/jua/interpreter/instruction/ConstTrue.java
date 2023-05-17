package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class ConstTrue implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("const_true");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.constTrue();
    }
}
