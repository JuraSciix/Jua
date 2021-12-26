package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public enum Astore implements State {

    INSTANCE;

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
