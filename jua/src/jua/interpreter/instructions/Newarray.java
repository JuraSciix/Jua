package jua.interpreter.instructions;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.ArrayOperand;
import jua.compiler.CodePrinter;

public enum Newarray implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("newarray");
    }

    @Override
    public int run(InterpreterState state) {
        state.pushStack(new ArrayOperand());
        return NEXT;
    }
}