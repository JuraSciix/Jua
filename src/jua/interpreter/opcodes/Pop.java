package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Pop implements Opcode {

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