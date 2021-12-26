package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class DuplicateMove implements State {

    public static final DuplicateMove DUP_MOV_1_M3 = new DuplicateMove(1, -3);

    private final int count;

    private final int x;

    public DuplicateMove(int count, int x) {
        this.count = count;
        this.x = x;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup_mov");
        printer.printOperand(count);
        printer.printOperand(x);
    }

    @Override
    public void run(Environment env) {
        env.duplicateStack(count, 1);
        env.moveStack(x);
        env.nextPC();
    }
}
