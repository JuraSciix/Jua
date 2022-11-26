package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

@Deprecated
public final class Clone implements Instruction {

    public static final Clone INSTANCE = new Clone();

    @Override
    public int stackAdjustment() { return -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("clone");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackClone();
    }
}