package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Vstore implements State {

    private final String name;

    private final int id;

    public Vstore(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vstore");
        printer.printIdentifier(name, id);
    }

    @Override
    public int run(Environment env) {
        env.setLocal(id, env.popStack());
        return NEXT;
    }
}