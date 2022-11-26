package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class ifconsteq extends JumpInstruction {

    private final short value;

    public ifconsteq(short value) {
        this.value = value;
    }

    public ifconsteq(int offset, short value) {
        super(offset);
        this.value = value;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new ifconstne(offset, value); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifconsteq");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public void run(InterpreterState state) {
        state.ifconsteq(value, offset);
    }
}