package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.interpreter.InterpreterError;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Shl implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("shl");
    }

    @Override
    public int run(InterpreterThread thread) {
        Operand rhs = thread.popStack();
        Operand lhs = thread.popStack();

        if (lhs.isLong() && rhs.isLong()) {
            thread.pushStack(lhs.longValue() << rhs.longValue());
        } else {
            throw InterpreterError.binaryApplication("<<", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}