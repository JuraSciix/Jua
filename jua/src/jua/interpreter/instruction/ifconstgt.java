package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class ifconstgt extends JumpInstruction {

    private final int value;

    public ifconstgt(int value) {
    this.value = value;
    }

    public ifconstgt(int offset, int value) {
        super(offset);
        this.value = value;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new ifconstle(offset, value); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifconstgt");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (state.popInt() > value) {
            return offset;
        } else {
            return NEXT;
        }
    }
}