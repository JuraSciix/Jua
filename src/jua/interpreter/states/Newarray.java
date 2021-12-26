package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.ArrayOperand;
import jua.tools.CodePrinter;

public enum Newarray implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("newarray");
    }

    @Override
    public int run(Environment env) {
        env.pushStack(new ArrayOperand());
        return NEXT;
    }
}