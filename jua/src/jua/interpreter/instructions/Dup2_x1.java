package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public enum Dup2_x1 implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2_x1");
    }

    @Override
    public int run(InterpreterThread thread) {
        thread.getFrame().getState().dup2_x1();
        return NEXT;
    }
}