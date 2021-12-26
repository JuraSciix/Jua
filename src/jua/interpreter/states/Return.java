package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.Trap;
import jua.interpreter.lang.NullOperand;
import jua.tools.CodePrinter;

public enum Return implements State {

    VOID(false),
    NOT_VOID(true);

    private final boolean passValue;

    Return(boolean passValue) {
        this.passValue = passValue;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName(passValue ? "return" : "return_null");
    }

    @Override
    public int run(Environment env) {
        env.exitCall(passValue ? env.popStack() : NullOperand.NULL);
        Trap.bti();
        return NEXT;
    }
}