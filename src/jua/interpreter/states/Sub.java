package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public enum Sub implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("sub");
    }

    @Override
    public int run(Environment env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isFloat() || rhs.isFloat()) {
                env.pushStack(lhs.floatValue() - rhs.floatValue());
            } else {
                env.pushStack(lhs.intValue() - rhs.intValue());
            }
        } else {
            throw InterpreterError.binaryApplication("-", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}