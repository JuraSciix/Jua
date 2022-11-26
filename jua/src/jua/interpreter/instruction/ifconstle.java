package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class ifconstle extends JumpInstruction {

    private final short value;

    public ifconstle(short value) {
        this.value = value;
    }

    public ifconstle(int offset, short value) {
        super(offset);
        this.value = value;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new ifconstgt(offset, value); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifconstle");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public void run(InterpreterState state) {
        state.ifconstle(value, offset);
    }
}