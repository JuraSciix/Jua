package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Pop implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.popStack();
        return NEXT;
    }
}