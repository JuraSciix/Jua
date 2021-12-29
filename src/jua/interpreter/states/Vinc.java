package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Vinc implements State {

    private final int id;

    public Vinc(int id) {
        this.id = id;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vinc");
        printer.printLocal(id);
    }

    @Override
    public int run(Environment env) {
        Operand local = env.getLocal(id);
        if (local == null) {
            env.getFrame().reportUndefinedVariable(id);
        } else {
            env.setLocal(id, local.inc());
        }
        return NEXT;
    }
}