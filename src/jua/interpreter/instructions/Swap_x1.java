package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterRuntime;

public final class Swap_x1 implements Instruction {
    public static final Swap_x1 SWAP_X1 = new Swap_x1();
    private Swap_x1() {
        super();
    }
    @Override
    public void print(CodePrinter printer) {
        printer.printName("swap_x1");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.getFrame().swap_x1();
        return NEXT;
    }
}
