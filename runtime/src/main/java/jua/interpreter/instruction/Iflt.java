package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Iflt extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negated() { return new Ifge().elsePoint(_elsePoint); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("iflt");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.iflt(_elsePoint);
    }
}