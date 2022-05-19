package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.interpreter.InterpreterError;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Div implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("div");
    }

    @Override
    public int run(InterpreterThread thread) {
        Operand rhs = thread.popStack();
        Operand lhs = thread.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                thread.pushStack(lhs.doubleValue() / rhs.doubleValue());
            } else {
                long l = lhs.longValue();
                long r = rhs.longValue();

                if (r == 0) {
                    throw InterpreterError.divisionByZero();
                }
                if ((l % r) == 0) {
                    thread.pushStack(l / r);
                } else {
                    thread.pushStack((double) l / r);
                }
            }
        } else {
            throw InterpreterError.binaryApplication("/", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}