package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public final class Vload implements Instruction {

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
    public int run(InterpreterThread thread) {
        Operand operand = thread.getLocal(id);
        thread.pushStack(operand);
        return NEXT;
    }
}