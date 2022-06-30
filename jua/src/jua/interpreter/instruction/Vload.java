package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
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
    public int run(InterpreterState state) {
        Operand operand = state.load(id);
        state.pushStack(operand);
        return NEXT;
    }
}