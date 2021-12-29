package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public enum Pop2 implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop2");
    }

    @Override
    public int run(Environment env) {
        env.popStack();
        env.popStack();
        return NEXT;
    }
}