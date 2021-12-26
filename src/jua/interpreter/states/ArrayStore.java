package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class ArrayStore implements State {

    public static final ArrayStore ASTORE = new ArrayStore();

    private ArrayStore() {
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
