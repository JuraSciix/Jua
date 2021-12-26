package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.Trap;
import jua.interpreter.lang.NullOperand;
import jua.tools.CodePrinter;

public enum Retnull implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("retnull");
    }

    @Override
    public int run(Environment env) {
        env.exitCall(NullOperand.NULL);
        Trap.bti();
        return NEXT;
    }
}