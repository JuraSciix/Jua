package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public enum Dup_x1 implements State {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup_x1");
    }

    @Override
    public int run(Environment env) {
        env.getProgram().dup1_x1();
        return NEXT;
    }
}