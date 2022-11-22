package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Inc implements Instruction {

    public static final Inc INSTANCE = new Inc();

    @Override
    public int stackAdjustment() { return -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("inc");
    }

    @Override
    public int run(InterpreterState state) {
        state.pushStack(state.popStack().increment());
        return NEXT;
    }
}