package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public enum Dec implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dec");
    }

    @Override
    public int run(Environment env) {
        env.pushStack(env.popStack().dec());
        return NEXT;
    }
}