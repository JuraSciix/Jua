package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ifnonnull extends JumpInstruction {

    public Ifnonnull() {
        super();
    }

    public Ifnonnull(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new Ifnull(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnonnull");
        super.print(printer);
    }

    @Override
    public void run(InterpreterState state) {
        state.ifnonnull(offset);
    }
}