package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public enum Pop implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop");
    }

    @Override
    public int run(Environment env) {
        env.popStack();
        return NEXT;
    }
}