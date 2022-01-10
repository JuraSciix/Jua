package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public class Ifne extends ChainInstruction {

    public static final Ifne IF_TRUE = new Ifne(0);

    private final int value;

    public Ifne(int value) {
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifne");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (env.popInt() != value) {
            return destination;
        } else {
            return NEXT;
        }
    }
}