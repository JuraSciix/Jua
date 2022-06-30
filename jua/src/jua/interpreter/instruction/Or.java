package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Or implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("or");
    }

    @Override
    public int run(InterpreterState state) {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.or(rhs));
        return NEXT;
    }
}