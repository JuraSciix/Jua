package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class IfAbsent extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1 + -1; }

    @Override
    public JumpInstruction negated() { return new IfPresent().elsePoint(_elsePoint); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("if_absent");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.if_absent(_elsePoint);
    }
}
