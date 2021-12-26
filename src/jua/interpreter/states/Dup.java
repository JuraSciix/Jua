package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public enum Dup implements State {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup");
    }

    @Override
    public int run(Environment env) {
        env.pushStack(env.peekStack());
        return NEXT;
    }
}