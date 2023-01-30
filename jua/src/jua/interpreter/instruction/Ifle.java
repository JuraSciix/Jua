package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ifle extends JumpInstruction {

    public Ifle() {
        super();
    }

    public Ifle(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negate() { return new Ifgt(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifle");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifle(offset);
    }
}