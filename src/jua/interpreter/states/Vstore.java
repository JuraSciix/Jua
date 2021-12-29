package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public final class Vstore implements State {

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
    public int run(Environment env) {
        env.setLocal(id, env.popStack());
        return NEXT;
    }
}