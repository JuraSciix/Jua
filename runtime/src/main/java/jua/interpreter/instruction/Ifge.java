package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Ifge extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negated() { return new Iflt().elsePoint(_elsePoint); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifge");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifge(_elsePoint);
    }
}