package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Leave implements Instruction {

    public static final Leave INSTANCE = new Leave();

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("leave");
    }

    @Override
    public void run(InterpreterState state) {
        state.thread().getReturnAddress().setNull();
        state.thread().set_returnee();
    }
}