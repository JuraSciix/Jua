package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public final class Ifle extends ChainOpcode {

    private final int value;

    public Ifle(int value) {
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifle");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (env.popInt() <= value) {
            return destination;
        } else {
            return NEXT;
        }
    }
}