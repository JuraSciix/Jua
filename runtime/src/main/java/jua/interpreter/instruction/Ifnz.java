package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

/**
 * If not zero (if is true)
 */
public class Ifnz extends JumpInstruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negated() { return new Ifz().elsePoint(_elsePoint); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnz");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifnz(_elsePoint);
    }
}
