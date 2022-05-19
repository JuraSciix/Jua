package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterThread;

public enum Length implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.print("length");
    }

    @Override
    public int run(InterpreterThread thread) {
        thread.pushStack(thread.popStack().length());
        return NEXT;
    }
}
