package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

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
    public int run(InterpreterRuntime env) {
        env.setLocal(id, env.popStack());
        return NEXT;
    }
}