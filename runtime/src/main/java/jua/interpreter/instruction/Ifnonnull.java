package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Ifnonnull extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negated() { return new Ifnull().elsePoint(_elsePoint); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnonnull");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifnonnull(_elsePoint);
    }
}