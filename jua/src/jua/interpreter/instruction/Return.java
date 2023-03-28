package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Return implements Instruction {

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("return");
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.impl_return();
    }
}