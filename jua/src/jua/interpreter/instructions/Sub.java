package jua.interpreter.instructions;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.DoubleOperand;
import jua.runtime.heap.LongOperand;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Sub implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("sub");
    }

    @Override
    public int run(InterpreterState state) {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.sub(rhs));
        return NEXT;
    }
}