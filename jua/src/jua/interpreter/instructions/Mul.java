package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Mul implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("mul");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                env.pushStack(lhs.doubleValue() * rhs.doubleValue());
            } else {
                env.pushStack(lhs.longValue() * rhs.longValue());
            }
        } else {
            throw InterpreterError.binaryApplication("*", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}