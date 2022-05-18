package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public enum Dup implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup");
    }

    @Override
    public int run(InterpreterThread env) {
        env.pushStack(env.peekStack());
        return NEXT;
    }
}