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

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                state.pushStack(new DoubleOperand(lhs.doubleValue() - rhs.doubleValue()));
            } else {
                state.pushStack(new LongOperand(lhs.longValue() - rhs.longValue()));
            }
        } else {
            throw InterpreterError.binaryApplication("-", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}