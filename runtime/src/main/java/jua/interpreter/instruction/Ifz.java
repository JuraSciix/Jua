package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Ifz extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negated() { return new Ifnz().elsePoint(_elsePoint); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifz");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifz(_elsePoint);
    }
}
