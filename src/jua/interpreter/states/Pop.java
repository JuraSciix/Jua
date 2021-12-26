package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Pop implements State {

    public static final Pop POP = new Pop();

    private Pop() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop");
    }

    @Override
    public void run(Environment env) {
        env.popStack();
        env.nextPC();
    }
}
