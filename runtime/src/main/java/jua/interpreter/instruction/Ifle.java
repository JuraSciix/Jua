package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Ifle extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negated() { return new Ifgt().elsePoint(_elsePoint); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifle");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifle(_elsePoint);
    }
}