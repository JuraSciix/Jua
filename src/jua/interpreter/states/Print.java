package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Print implements State {

    protected final int count;

    public Print(int count) {
        this.count = count;
    }

    protected String getString(Environment env) {
        String[] values = new String[count];

        for (int i = count; --i >= 0; ) {
            values[i] = env.popString();
        }
        return String.join("", values);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("print");
        printer.printOperand(count);
    }

    @Override
    public int run(Environment env) {
        System.out.print(getString(env));
        return NEXT;
    }
}