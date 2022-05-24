package jua.interpreter.instructions;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Not implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("not");
    }

    @Override
    public int run(InterpreterState state) {
        Operand val = state.popStack();
        state.pushStack(val.not());
        return NEXT;
    }
}