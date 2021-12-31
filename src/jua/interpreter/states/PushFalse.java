package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.FalseOperand;
import jua.tools.CodePrinter;

@Deprecated
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
    public int run(Environment env) {
        env.pushStack(FalseOperand.FALSE);
        return NEXT;
    }
}