package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public final class Ifeq extends JumpState {

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
    public int run(Environment env) {
        if (env.popInt() == value) {
            return destination;
        } else {
            return NEXT;
        }
    }
}