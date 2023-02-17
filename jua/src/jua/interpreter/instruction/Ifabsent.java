package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public class Ifabsent extends JumpInstruction {

    public Ifabsent(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negate() { return new Ifpresent(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifabsent");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        throw new AssertionError(this); // todo
    }
}
