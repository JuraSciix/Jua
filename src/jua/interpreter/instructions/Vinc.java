package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.Operand;
import jua.compiler.CodePrinter;

public class Vinc implements Instruction {

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
    public int run(InterpreterRuntime env) {
        Operand local = env.getLocal(id);
        if (local == null) {
            env.getFrame().reportUndefinedVariable(id);
        } else {
            env.setLocal(id, local.inc());
        }
        return NEXT;
    }
}