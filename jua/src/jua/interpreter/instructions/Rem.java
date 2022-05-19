package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Rem implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("rem");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                double r = rhs.doubleValue();

                if (r == 0D) {
                    throw InterpreterError.divisionByZero();
                }
                env.pushStack(lhs.doubleValue() % r);
            } else {
                long r = rhs.longValue();

                if (r == 0L) {
                    throw InterpreterError.divisionByZero();
                }
                env.pushStack(lhs.longValue() % r);
            }
        } else {
            throw InterpreterError.binaryApplication("%", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}