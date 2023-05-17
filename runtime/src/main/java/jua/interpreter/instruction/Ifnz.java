package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

/**
 * If not zero (if is true)
 */
public class Ifnz extends JumpInstruction {

    public Ifnz() {
        super();
    }

    public Ifnz(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new Ifz(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnz");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifnz(offset);
    }
}
