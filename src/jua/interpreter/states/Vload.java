package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public final class Vload implements State {

    private final int id;

    public Vload(int id) {
        this.id = id;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vload");
        printer.printLocal(id);
    }

    @Override
    public int run(Environment env) {
        Operand operand = env.getLocal(id);

        if (operand == null) {
            env.getFrame().reportUndefinedVariable(id);
        } else {
            env.pushStack(operand);
        }
        return NEXT;
    }
}