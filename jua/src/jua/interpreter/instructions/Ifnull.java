package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
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
    public int run(InterpreterRuntime env) {
        if (env.popStack().isNull()) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}