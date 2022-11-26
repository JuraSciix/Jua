package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class ifconstge extends JumpInstruction {

    private final short value;

    public ifconstge(short value) {
        this.value = value;
    }

    public ifconstge(int offset, short value) {
        super(offset);
        this.value = value;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new ifconstlt(offset, value); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifconstge");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public void run(InterpreterState state) {
        state.ifconstge(value, offset);
    }
}