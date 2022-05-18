package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public final class Vstore implements Instruction {

    private final int id;

    public Vstore(int id) {
        this.id = id;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vstore");
        printer.printLocal(id);
    }

    @Override
    public int run(InterpreterThread env) {
        env.setLocal(id, env.popStack());
        return NEXT;
    }
}