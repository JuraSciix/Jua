package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public enum Inc implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("inc");
    }

    @Override
    public int run(Environment env) {
        env.pushStack(env.popStack().inc());
        return NEXT;
    }
}