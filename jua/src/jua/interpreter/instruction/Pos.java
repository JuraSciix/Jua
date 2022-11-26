package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Pos implements Instruction {

    public static final Pos INSTANCE = new Pos();

    @Override
    public int stackAdjustment() { return -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pos");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackPos();
    }
}