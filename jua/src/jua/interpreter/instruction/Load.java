package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Load implements Instruction {

    private final int id;

    public Load(int id) {
        this.id = id;
    }

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("load");
        printer.printLocal(id);
    }

    @Override
    public void run(InterpreterState state) {
        state.stackVLoad(id);
    }
}