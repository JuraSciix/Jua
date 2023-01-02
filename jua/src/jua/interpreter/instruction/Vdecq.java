package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Vdecq implements Instruction {

    private final int id;

    public Vdecq(int id) {
        this.id = id;
    }

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vdecq");
        printer.printLocal(id);
    }

    @Override
    public void run(InterpreterState state) {
        state.stack_quick_vdec(id);
    }
}