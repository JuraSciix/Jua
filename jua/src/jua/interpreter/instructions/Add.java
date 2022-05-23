package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.*;

public enum Add implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("add");
    }

    @Override
    public int run(InterpreterState state) {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (lhs.isNumber() && rhs.isNumber() || (lhs.isString() || rhs.isString())) {
            state.pushStack(lhs.add(rhs));
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