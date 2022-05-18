package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public final class Ifge extends ChainInstruction {

    private final int value;

    public Ifge(int destIp, int value) {
        super(destIp);
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifge");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterThread env) {
        if (env.popInt() >= value) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}