package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public enum Dec implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dec");
    }

    @Override
    public int run(InterpreterThread env) {
        env.pushStack(env.popStack().decrement());
        return NEXT;
    }
}