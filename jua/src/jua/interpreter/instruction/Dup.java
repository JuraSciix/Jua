package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public enum Dup implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup");
    }

    @Override
    public int run(InterpreterState state) {
        state.dup();
        return NEXT;
    }
}