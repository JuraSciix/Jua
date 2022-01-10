package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.interpreter.runtime.Operand;
import jua.tools.CodePrinter;

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
            if (lhs.isFloat() || rhs.isFloat()) {
                double r = rhs.floatValue();

                if (r == 0D) {
                    throw InterpreterError.divisionByZero();
                }
                env.pushStack(lhs.floatValue() % r);
            } else {
                long r = rhs.intValue();

                if (r == 0L) {
                    throw InterpreterError.divisionByZero();
                }
                env.pushStack(lhs.intValue() % r);
            }
        } else {
            throw InterpreterError.binaryApplication("%", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}