package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public enum Dup_x1 implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup_x1");
    }

    @Override
    public int run(InterpreterThread thread) {
        thread.getFrame().getState().dup1_x1();
        return NEXT;
    }
}