package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public enum Aload implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("aload");
    }

    @Override
    public int run(Environment env) {
        Operand key = env.popStack();
        env.pushStack(env.popArray().get(key));
        return NEXT;
    }
}