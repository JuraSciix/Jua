package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

// todo: Переименовать в ifnz
public final class ifnz extends JumpInstruction {

    public ifnz() {
        super();
    }

    public ifnz(int offset) {
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
    public void run(InterpreterState state) {
        state.ifnz(offset);
    }
}
