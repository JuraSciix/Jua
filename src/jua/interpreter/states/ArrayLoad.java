package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class ArrayLoad implements State {

    public static final ArrayLoad ALOAD = new ArrayLoad();

    private ArrayLoad() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("aload");
    }

    @Override
    public void run(Environment env) {
        Operand key = env.popStack();
        env.pushStack(env.popArray().get(key));
        env.nextPC();
    }
}
