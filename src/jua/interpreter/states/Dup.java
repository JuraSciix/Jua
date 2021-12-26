package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Dup implements State {

    public static final Dup DUP1_1 = new Dup(1, 1);
    public static final Dup DUP1_2 = new Dup(1, 2);
    public static final Dup DUP2_1 = new Dup(2, 1);

    private final int count;

    private final int x;

    public Dup(int count, int x) {
        this.count = count;
        this.x = x;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup");
        printer.printOperand(count);
        printer.printOperand(x);
    }

    @Override
    public void run(Environment env) {
        env.duplicateStack(count, x);
        env.nextPC();
    }
}
