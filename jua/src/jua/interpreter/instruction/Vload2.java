package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

// todo: rename to Load (vload -> load)
public final class Vload2 implements Instruction {

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vload2");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackVLoad(2);
    }
}