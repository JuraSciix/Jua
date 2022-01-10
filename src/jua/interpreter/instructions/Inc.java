package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Inc implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("inc");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack(env.popStack().inc());
        return NEXT;
    }
}