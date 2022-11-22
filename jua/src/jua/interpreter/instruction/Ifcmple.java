package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.compiler.CodePrinter;

public final class Ifcmple extends JumpInstruction {

    public Ifcmple() {
        super();
    }

    public Ifcmple(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negate() { return new Ifcmpgt(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmple");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (!state.stackCmple()) {
           return NEXT;
        } else {
            return offset;
        }
    }
}