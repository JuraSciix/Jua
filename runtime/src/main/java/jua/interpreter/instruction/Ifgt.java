package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Ifgt extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negated() { return new Ifle().elsePoint(_elsePoint); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifgt");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifgt(_elsePoint);
    }
}