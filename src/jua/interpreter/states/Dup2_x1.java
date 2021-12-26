package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public enum Dup2_x1 implements State {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2_x1");
    }

    @Override
    public int run(Environment env) {
        env.getFrame().dup2_x2();
        return NEXT;
    }
}