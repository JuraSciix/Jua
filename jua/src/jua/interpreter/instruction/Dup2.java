package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Dup2 implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2");
    }

    @Override
    public int run(InterpreterState state) {
        Operand a = state.popStack();
        Operand b = state.popStack();
        state.pushStack(b);
        state.pushStack(a);
        state.pushStack(b);
        state.pushStack(a);
        return NEXT;
    }
}