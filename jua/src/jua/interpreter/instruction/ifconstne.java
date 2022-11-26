package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class ifconstne extends JumpInstruction {

    private final short value;

    public ifconstne(short value) {
        this.value = value;
    }

    public ifconstne(int offset, short value) {
        super(offset);
        this.value = value;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new ifconsteq(offset, value); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifconstne");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public void run(InterpreterState state) {
        state.ifconstne(value, offset);
    }
}