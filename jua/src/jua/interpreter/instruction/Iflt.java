package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Iflt extends ChainInstruction {

    private final int value;

    public Iflt(int destIp, int value) {
        super(destIp);
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("iflt");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (state.popInt() < value) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}