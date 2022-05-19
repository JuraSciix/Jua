package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Xor implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("xor");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isLong() && rhs.isLong()) {
            env.pushStack(lhs.longValue() ^ rhs.longValue());
        } else if (lhs.isBoolean() && rhs.isBoolean()) {
            env.pushStack(lhs.booleanValue() ^ rhs.booleanValue());
        } else {
            throw InterpreterError.binaryApplication("^", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}