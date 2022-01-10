package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public final class Ifge extends ChainInstruction {

    private final int value;

    public Ifge(int value) {
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifge");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (env.popInt() >= value) {
            return destination;
        } else {
            return NEXT;
        }
    }
}