package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

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
    public int run(InterpreterState state) {
        if (state.popStack().isNull()) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}