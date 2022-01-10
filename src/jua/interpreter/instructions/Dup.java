package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Dup implements Instruction {

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