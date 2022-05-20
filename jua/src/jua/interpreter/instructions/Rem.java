package jua.interpreter.instructions;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.DoubleOperand;
import jua.runtime.LongOperand;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Rem implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("rem");
    }

    @Override
    public int run(InterpreterState state) {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                double r = rhs.doubleValue();

                if (r == 0D) {
                    throw InterpreterError.divisionByZero();
                }
                state.pushStack(new DoubleOperand(lhs.doubleValue() % r));
            } else {
                long r = rhs.longValue();

                if (r == 0L) {
                    throw InterpreterError.divisionByZero();
                }
                state.pushStack(new LongOperand(lhs.longValue() % r));
            }
        } else {
            throw InterpreterError.binaryApplication("%", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}