package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Goto extends JumpInstruction {

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public JumpInstruction negated() {
        throw new UnsupportedOperationException(getClass().getName());
    }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("goto");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state._goto(offsetJump);
    }
}