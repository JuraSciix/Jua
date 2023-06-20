package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Ifge extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negated() { return new Iflt().offsetJump(offsetJump); }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("ifge");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifge(offsetJump);
    }
}