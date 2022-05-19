package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterRuntime;

public enum Length implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.print("length");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack(env.popStack().length());
        return NEXT;
    }
}
