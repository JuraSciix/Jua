package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.interpreter.runtime.Array;
import jua.interpreter.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Add implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("add");
    }

    @Override
    public int run(InterpreterRuntime env) {
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
            Array value = new Array();
            value.setAll(lhs.arrayValue());
            value.setAll(rhs.arrayValue());
            env.pushStack(value);
        } else {
            throw InterpreterError.binaryApplication("+", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}