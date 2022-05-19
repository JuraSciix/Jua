package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

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
    public int run(InterpreterThread thread) {
        Operand local = thread.getLocal(id);
        thread.setLocal(id, local.decrement());
        return NEXT;
    }
}