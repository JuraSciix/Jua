package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

// todo: rename to Store (vstore -> store)
public final class Vstore1 implements Instruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vstore1");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackVStore(1);
    }
}