package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class IfAbsent extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negated() { return new IfPresent().offsetJump(offsetJump); }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("if_absent");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.if_absent(offsetJump);
    }
}
