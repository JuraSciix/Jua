package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ifnull extends JumpInstruction {

    public Ifnull() {
        super();
    }

    public Ifnull(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new Ifnonnull(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnull");
        super.print(printer);
    }

    @Override
    public void run(InterpreterState state) {
        state.ifnull(offset);
    }
}