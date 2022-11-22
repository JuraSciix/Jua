package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.compiler.CodePrinter;

public final class Ifcmplt extends JumpInstruction {

    public Ifcmplt() {
        super();
    }

    public Ifcmplt(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negate() { return new Ifcmpge(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmplt");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (!state.stackCmplt()) {
            return NEXT;
        } else {
            return offset;
        }
    }
}