package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public enum Clone implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("clone");
    }

    @Override
    public int run(Environment env) {
        env.pushStack((Operand) env.popStack().clone());
        return NEXT;
    }
}