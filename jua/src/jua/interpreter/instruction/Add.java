package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.*;

public enum Add implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("add");
    }

    @Override
    public int run(InterpreterState state) {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.add(rhs));
        return NEXT;
    }
}