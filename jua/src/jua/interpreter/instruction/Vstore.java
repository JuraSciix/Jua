package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Vstore implements Instruction {

    private final int id;

    public Vstore(int id) {
        this.id = id;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vstore");
        printer.printLocal(id);
    }

    @Override
    public int run(InterpreterState state) {
        state.stackVStore(id);
        return NEXT;
    }
}