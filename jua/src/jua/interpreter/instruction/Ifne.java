package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ifne extends JumpInstruction {

    public Ifne() {
        super();
    }

    public Ifne(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negate() { return new Ifeq(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifne");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (!state.stackCmpeq()) {
            return offset;
        } else {
            return NEXT;
        }
    }
}