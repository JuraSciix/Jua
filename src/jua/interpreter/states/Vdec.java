package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Vdec implements State {

    private final String name;

    private final int id;

    public Vdec(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vdec");
        printer.printIdentifier(name, id);
    }

    @Override
    public int run(Environment env) {
        env.setLocal(id, env.getLocal(id).dec());
        return NEXT;
    }
}