package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Pop2 implements State {

    public static final Pop2 POP2 = new Pop2();

    private Pop2() {
        super();
    }

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