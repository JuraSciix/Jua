package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public enum Pop implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop");
    }

    @Override
    public int run(InterpreterState state) {
        state.popStack();
        return NEXT;
    }
}