package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Astore implements State {

    public static final Astore ASTORE = new Astore();

    private Astore() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("astore");
    }

    @Override
    public void run(Environment env) {
        Operand val = env.popStack();
        Operand key = env.popStack();
        env.popArray().set(key, val);
        env.nextPC();
    }
}
