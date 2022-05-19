package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public final class Ifnull extends ChainInstruction {

    public Ifnull(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnull");
        super.print(printer);
    }

    @Override
    public int run(InterpreterThread thread) {
        if (thread.popStack().isNull()) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}