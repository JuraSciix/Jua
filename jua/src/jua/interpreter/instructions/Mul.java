package jua.interpreter.instructions;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.DoubleOperand;
import jua.runtime.LongOperand;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Mul implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("mul");
    }

    @Override
    public int run(InterpreterState state) {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                state.pushStack(new DoubleOperand(lhs.doubleValue() * rhs.doubleValue()));
            } else {
                state.pushStack(new LongOperand(lhs.longValue() * rhs.longValue()));
            }
        } else {
            throw InterpreterError.binaryApplication("*", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}