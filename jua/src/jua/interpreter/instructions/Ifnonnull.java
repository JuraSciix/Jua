package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public final class Ifnonnull extends ChainInstruction {

    public Ifnonnull(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnonnull");
        super.print(printer);
    }

    @Override
    public int run(InterpreterThread thread) {
        if (!thread.popStack().isNull()) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}