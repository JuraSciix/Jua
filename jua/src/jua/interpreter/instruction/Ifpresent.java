package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public class Ifpresent extends JumpInstruction {

    public Ifpresent() {
        super();
    }

    public Ifpresent(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negate() { return new Ifabsent(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifpresent");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        throw new AssertionError(this); // todo
    }
}
