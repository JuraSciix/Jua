package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public enum Clone implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("clone");
    }

    @Override
    public int run(InterpreterThread thread) {
        thread.pushStack(thread.popStack().doClone());
        return NEXT;
    }
}