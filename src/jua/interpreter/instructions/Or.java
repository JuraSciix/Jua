package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.interpreter.runtime.Operand;
import jua.tools.CodePrinter;

public enum Or implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("or");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isInt() && rhs.isInt()) {
            env.pushStack(lhs.intValue() | rhs.intValue());
        } else if (lhs.isBoolean() && rhs.isBoolean()) {
            env.pushStack(lhs.booleanValue() | rhs.booleanValue());
        } else {
            throw InterpreterError.binaryApplication("|", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}