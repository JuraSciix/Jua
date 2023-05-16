package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public final class Goto extends JumpInstruction {

    public Goto() {
        super();
    }

    public Goto(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() {
        return 0;
    }

    @Override
    public JumpInstruction negate() {
        return this; // todo
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("goto");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state._goto(offset);
    }
}