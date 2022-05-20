package jua.interpreter.instructions;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.DoubleOperand;
import jua.runtime.heap.LongOperand;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Div implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("div");
    }

    @Override
    public int run(InterpreterState state) {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                state.pushStack(new DoubleOperand(lhs.doubleValue() / rhs.doubleValue()));
            } else {
                long l = lhs.longValue();
                long r = rhs.longValue();

                if (r == 0) {
                    throw InterpreterError.divisionByZero();
                }
                if ((l % r) == 0) {
                    state.pushStack(new LongOperand(l / r));
                } else {
                    state.pushStack(new DoubleOperand((double) l / r));
                }
            }
        } else {
            throw InterpreterError.binaryApplication("/", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}