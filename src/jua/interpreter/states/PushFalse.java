package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.FalseOperand;
import jua.tools.CodePrinter;

public class PushFalse implements State {

    public static final PushFalse INSTANCE = new PushFalse();

    private PushFalse() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("push_false");
    }

    @Override
    public void run(Environment env) {
        env.pushStack(FalseOperand.FALSE);
        env.nextPC();
    }
}
