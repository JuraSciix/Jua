package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public enum Div implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("div");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackDiv();
        return NEXT;
    }
}