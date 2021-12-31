package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Dup implements Opcode {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack(env.peekStack());
        return NEXT;
    }
}