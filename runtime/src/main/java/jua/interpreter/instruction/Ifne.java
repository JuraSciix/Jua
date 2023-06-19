package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Ifne extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negated() { return new Ifeq().elsePoint(_elsePoint); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifne");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifne(_elsePoint);
    }
}