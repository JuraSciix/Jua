package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

// todo: Переименовать в ifnz
public final class Iftrue extends JumpInstruction {

    public Iftrue() {
        super();
    }

    public Iftrue(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new Iffalse(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("iftrue");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        return state.popStack().booleanValue() ? offset : NEXT;
    }
}
