package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.interpreter.InterpreterError;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Sub implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("sub");
    }

    @Override
    public int run(InterpreterThread thread) {
        Operand rhs = thread.popStack();
        Operand lhs = thread.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                thread.pushStack(lhs.doubleValue() - rhs.doubleValue());
            } else {
                thread.pushStack(lhs.longValue() - rhs.longValue());
            }
        } else {
            throw InterpreterError.binaryApplication("-", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}