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
    public int run(InterpreterThread thread) {
        thread.pushStack(thread.popStack().increment());
        return NEXT;
    }
}