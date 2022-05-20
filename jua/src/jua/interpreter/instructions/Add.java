package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.*;

public enum Add implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("add");
    }

    @Override
    public int run(InterpreterState state) {
        // todo: Распределить код по наследникам Operand.
        // todo: Также с остальными операциями над операндами.
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                state.pushStack(new DoubleOperand(lhs.doubleValue() + rhs.doubleValue()));
            } else {
                state.pushStack(new LongOperand(lhs.longValue() + rhs.longValue()));
            }
        } else if (lhs.isString() || rhs.isString()) {
            state.pushStack(new StringOperand(lhs.stringValue().concat(rhs.stringValue())));
        } else if (lhs.isMap() && rhs.isMap()) {
            ArrayOperand result = new ArrayOperand();
            result.putAll(lhs);
            result.putAll(rhs);
            state.pushStack(result);
        } else {
            throw InterpreterError.binaryApplication("+", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}