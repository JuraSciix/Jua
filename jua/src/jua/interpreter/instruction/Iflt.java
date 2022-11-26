package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.compiler.CodePrinter;

public final class Iflt extends JumpInstruction {

    public Iflt() {
        super();
    }

    public Iflt(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negate() { return new Ifge(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("iflt");
        super.print(printer);
    }

    @Override
    public void run(InterpreterState state) {
        state.iflt(offset);
    }
}