package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Goto extends JumpInstruction {

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public JumpInstruction negated() {
        throw new AssertionError("goto doesn't have a negative instruction yet");
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("goto");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state._goto(_elsePoint);
    }
}