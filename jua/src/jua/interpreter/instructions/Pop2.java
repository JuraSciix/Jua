package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public enum Pop2 implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop2");
    }

    @Override
    public int run(InterpreterThread env) {
        env.popStack();
        env.popStack();
        return NEXT;
    }
}