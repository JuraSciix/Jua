package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Println extends Print {

    public Println(int count) {
        super(count);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("println");
        printer.printOperand(count);
    }

    @Override
    public void run(Environment env) {
        System.out.println(getString(env));
        env.nextPC();
    }
}
