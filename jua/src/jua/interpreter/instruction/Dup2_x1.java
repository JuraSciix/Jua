package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public enum Dup2_x1 implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2_x1");
    }

    @Override
    public int run(InterpreterState state) {
        state.dup2_x1();
        return NEXT;
    }
}