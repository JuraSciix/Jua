package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Iflt extends JumpInstruction {

    private final int value;

    public Iflt(int value) {
        this.value = value;
    }

    public Iflt(int offset, int value) {
        super(offset);
        this.value = value;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() {
        return new Ifge(offset, value);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("iflt");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (state.popInt() < value) {
            return offset;
        } else {
            return NEXT;
        }
    }
}