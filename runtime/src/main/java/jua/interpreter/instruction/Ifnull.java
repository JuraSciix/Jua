package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Ifnull extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negated() { return new Ifnonnull().offsetJump(offsetJump); }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("ifnull");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifnull(offsetJump);
    }
}