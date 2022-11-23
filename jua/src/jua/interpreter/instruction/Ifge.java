package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.compiler.CodePrinter;

public final class Ifge extends JumpInstruction {

    public Ifge() {
        super();
    }

    public Ifge(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negate() { return new Iflt(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifge");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (!state.stackCmpge()) {
            return NEXT;
        } else {
            return offset;
        }
    }
}