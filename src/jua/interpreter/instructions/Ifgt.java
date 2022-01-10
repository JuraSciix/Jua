package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public final class Ifgt extends ChainInstruction {

    private final int value;

    public Ifgt(int value) {
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifgt");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (env.popInt() > value) {
            return destination;
        } else {
            return NEXT;
        }
    }
}