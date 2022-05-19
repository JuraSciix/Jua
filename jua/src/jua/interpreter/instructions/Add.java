package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterThread;
import jua.runtime.ArrayOperand;
import jua.runtime.Operand;

public enum Add implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("add");
    }

    @Override
    public int run(InterpreterThread thread) {
        // todo: Распределить код по наследникам Operand.
        // todo: Также с остальными операциями над операндами.
        Operand rhs = thread.popStack();
        Operand lhs = thread.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                thread.pushStack(lhs.doubleValue() + rhs.doubleValue());
            } else {
                thread.pushStack(lhs.longValue() + rhs.longValue());
            }
        } else if (lhs.isString() || rhs.isString()) {
            thread.pushStack(lhs.stringValue().concat(rhs.stringValue()));
        } else if (lhs.isMap() && rhs.isMap()) {
            ArrayOperand result = new ArrayOperand();
            result.putAll(lhs);
            result.putAll(rhs);
            thread.pushStack(result);
        } else {
            throw InterpreterError.binaryApplication("+", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}