package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public enum Dup2 implements State {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2");
    }

    @Override
    public int run(Environment env) {
        Operand a = env.popStack();
        Operand b = env.popStack();
        env.pushStack(b);
        env.pushStack(a);
        env.pushStack(b);
        env.pushStack(a);
        return NEXT;
    }
}