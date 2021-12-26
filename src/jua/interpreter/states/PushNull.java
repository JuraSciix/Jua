package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.NullOperand;
import jua.tools.CodePrinter;

public class PushNull implements State {

    public static final PushNull INSTANCE = new PushNull();

    private PushNull() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("push_null");
    }

    @Override
    public void run(Environment env) {
        env.pushStack(NullOperand.NULL);
        env.nextPC();
    }
}
