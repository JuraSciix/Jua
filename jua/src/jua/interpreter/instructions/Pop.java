package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public enum Pop implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop");
    }

    @Override
    public int run(InterpreterThread thread) {
        thread.popStack();
        return NEXT;
    }
}