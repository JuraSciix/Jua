package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Return implements State {

    public static final Return VOID = new Return(false);
    public static final Return NOT_VOID = new Return(true);

    private final boolean passValue;

    private Return(boolean passValue) {
        this.passValue = passValue;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName(passValue ? "return" : "return_null");
    }

    @Override
    public void run(Environment env) {
        env.exitCall(passValue);
        env.nextPC();
    }
}
