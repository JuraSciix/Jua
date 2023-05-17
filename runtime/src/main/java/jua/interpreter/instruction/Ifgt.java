package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Ifgt extends JumpInstruction {

    public Ifgt() {
        super();
    }

    public Ifgt(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negate() { return new Ifle(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifgt");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifgt(offset);
    }
}