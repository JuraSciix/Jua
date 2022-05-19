package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
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
    public int run(InterpreterRuntime env) {
        if (!env.popStack().isNull()) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}