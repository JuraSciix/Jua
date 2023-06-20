package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class IfPresent extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negated() { return new IfAbsent().offsetJump(offsetJump); }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("if_present");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.if_present(offsetJump);
    }
}
