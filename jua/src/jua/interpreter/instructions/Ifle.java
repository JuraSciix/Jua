package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public final class Ifle extends ChainInstruction {

    private final int value;

    public Ifle(int destIp, int value) {
        super(destIp);
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifle");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterThread thread) {
        if (thread.popInt() <= value) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}