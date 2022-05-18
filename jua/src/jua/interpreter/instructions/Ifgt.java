package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public final class Ifgt extends ChainInstruction {

    private final int value;

    public Ifgt(int destIp, int value) {
        super(destIp);
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifgt");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterThread env) {
        if (env.popInt() > value) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}