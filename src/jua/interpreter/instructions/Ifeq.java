package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public final class Ifeq extends ChainInstruction {

    public static final Ifeq IF_FALSE = new Ifeq(0);

    private final int value;

    public Ifeq(int value) {
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifeq");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (env.popInt() == value) {
            return destination;
        } else {
            return NEXT;
        }
    }
}