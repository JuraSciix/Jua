package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

// todo: rename to Load (vload -> load)
public final class Load1 implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("load_1");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackVLoad(1);
    }
}