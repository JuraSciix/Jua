package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.Trap;
import jua.interpreter.lang.NullOperand;
import jua.tools.CodePrinter;

public enum Return implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("return");
    }

    @Override
    public int run(Environment env) {
        env.exitCall(env.popStack());
        env.getFrame().incPC();
        Trap.bti();
        return 0; // unreachable
    }
}