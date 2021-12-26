package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.TrueOperand;
import jua.tools.CodePrinter;

public class PushTrue implements State {

    public static final PushTrue INSTANCE = new PushTrue();

    private PushTrue() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("push_true");
    }

    @Override
    public void run(Environment env) {
        env.pushStack(TrueOperand.TRUE);
        env.nextPC();
    }
}
