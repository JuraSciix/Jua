package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterRuntime;

public final class Swap implements Instruction {


    public static final Swap
    SWAP = new Swap();

    private Swap() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("swap");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.getFrame().swap();
        return NEXT;
    }
}
