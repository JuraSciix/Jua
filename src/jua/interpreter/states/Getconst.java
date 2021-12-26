package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Getconst implements State {

    private final String name;

    public Getconst(String name) {
        this.name = name;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("get_const");
        printer.printOperand(name);
    }

    @Override
    public void run(Environment env) {
        env.pushStack(env.getConstantByName(name).value);
        env.nextPC();
    }
}
