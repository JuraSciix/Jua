package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public final class Println extends Print {

    public Println(int count) {
        super(count);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("println");
        printer.print(count);
    }

    @Override
    public int run(Environment env) {
        super.run(env);
        System.out.println();
        return NEXT;
    }
}