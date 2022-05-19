package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.compiler.CodePrinter;

public enum Dup2_x1 implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2_x1");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.getFrame().getState().dup2_x1();
        return NEXT;
    }
}