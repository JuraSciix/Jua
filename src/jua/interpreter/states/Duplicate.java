package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Duplicate implements State {

    public static final Duplicate DUP1_1 = new Duplicate(1, 1);
    public static final Duplicate DUP1_2 = new Duplicate(1, 2);
    public static final Duplicate DUP2_1 = new Duplicate(2, 1);

    private final int count;

    private final int x;

    public Duplicate(int count, int x) {
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
