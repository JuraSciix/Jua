package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ifne extends JumpInstruction {

    private final int value;

    public Ifne(int value) {
        this.value = value;
    }

    public Ifne(int offset, int value) {
        super(offset);
        this.value = value;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new Ifeq(offset, value); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifne");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (state.popInt() != value) {
            return offset;
        } else {
            return NEXT;
        }
    }
}