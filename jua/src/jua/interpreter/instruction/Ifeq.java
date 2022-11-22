package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ifeq extends JumpInstruction {

    private final int value;

    public Ifeq(int value) {
        this.value = value;
    }

    public Ifeq(int offset, int value) {
        super(offset);
        this.value = value;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new Ifne(offset, value); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifeq");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (state.popInt() == value) {
            return offset;
        } else {
            return NEXT;
        }
    }
}