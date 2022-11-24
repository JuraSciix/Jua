package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class ifconstlt extends JumpInstruction {

    private final short value;

    public ifconstlt(short value) {
        this.value = value;
    }

    public ifconstlt(int offset, short value) {
        super(offset);
        this.value = value;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() {
        return new ifconstge(offset, value);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifconstlt");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (state.popStack().compareShort(value, 1) < 0) {
            return offset;
        } else {
            return NEXT;
        }
    }
}