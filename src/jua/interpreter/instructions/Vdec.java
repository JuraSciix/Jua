package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.Operand;
import jua.tools.CodePrinter;

public final class Vdec implements Instruction {

    private final int id;

    public Vdec(int id) {
        this.id = id;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vdec");
        printer.printLocal(id);
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand local = env.getLocal(id);
        if (local == null) {
            env.getFrame().reportUndefinedVariable(id);
        } else {
            env.setLocal(id, local.dec());
        }
        return NEXT;
    }
}