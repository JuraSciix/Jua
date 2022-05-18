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
    public int run(InterpreterThread env) {
        // todo: Распределить код по наследникам Operand.
        // todo: Также с остальными операциями над операндами.
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isDouble() || rhs.isDouble()) {
                env.pushStack(lhs.doubleValue() + rhs.doubleValue());
            } else {
                env.pushStack(lhs.longValue() + rhs.longValue());
            }
        } else if (lhs.isString() || rhs.isString()) {
            env.pushStack(lhs.stringValue().concat(rhs.stringValue()));
        } else if (lhs.isMap() && rhs.isMap()) {
            ArrayOperand result = new ArrayOperand();
            result.putAll(lhs);
            result.putAll(rhs);
            env.pushStack(result);
        } else {
            throw InterpreterError.binaryApplication("+", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}