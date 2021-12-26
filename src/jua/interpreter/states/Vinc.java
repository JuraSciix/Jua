package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Vinc implements State {

    private final String name;
    private final int id;

    public Vinc(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vinc");
        printer.printIdentifier(name, id);
    }

    @Override
    public void run(Environment env) {
        env.setLocal(id, env.getLocal(id).inc());
        env.nextPC();
    }
}
