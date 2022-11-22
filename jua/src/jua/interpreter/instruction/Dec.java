package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Dec implements Instruction {

    public static final Dec INSTANCE = new Dec();

    @Override
    public int stackAdjustment() { return -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dec");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackDec();
        return NEXT;
    }
}