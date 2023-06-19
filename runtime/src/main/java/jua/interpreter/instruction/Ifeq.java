package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Ifeq extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negated() { return new Ifne().elsePoint(_elsePoint); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifeq");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifeq(_elsePoint);
    }
}