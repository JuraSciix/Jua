package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.interpreter.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Div implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("div");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                env.pushStack(lhs.doubleValue() / rhs.doubleValue());
            } else {
                long l = lhs.longValue();
                long r = rhs.longValue();

                if (r == 0) {
                    throw InterpreterError.divisionByZero();
                }
                if ((l % r) == 0) {
                    env.pushStack(l / r);
                } else {
                    env.pushStack((double) l / r);
                }
            }
        } else {
            throw InterpreterError.binaryApplication("/", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}