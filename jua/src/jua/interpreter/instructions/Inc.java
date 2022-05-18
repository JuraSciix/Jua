package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public enum Inc implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("inc");
    }

    @Override
    public int run(InterpreterThread env) {
        env.pushStack(env.popStack().increment());
        return NEXT;
    }
}