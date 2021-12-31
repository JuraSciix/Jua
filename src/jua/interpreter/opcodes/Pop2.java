package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Pop2 implements Opcode {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pop2");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.popStack();
        env.popStack();
        return NEXT;
    }
}