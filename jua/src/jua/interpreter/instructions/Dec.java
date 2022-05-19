package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.compiler.CodePrinter;

public enum Dec implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dec");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack(env.popStack().decrement());
        return NEXT;
    }
}