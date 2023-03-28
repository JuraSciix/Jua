package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class Ifeq extends JumpInstruction {

    public Ifeq() {
        super();
    }

    public Ifeq(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negate() { return new Ifne(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifeq");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifeq(offset);
    }
}