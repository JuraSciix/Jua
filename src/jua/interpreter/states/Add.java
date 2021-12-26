package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Array;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Add implements State {

    public static final Add ADD = new Add();

    private Add() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("add");
    }

    @Override
    public void run(Environment env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isFloat() || rhs.isFloat()) {
                env.pushStack(lhs.floatValue() + rhs.floatValue());
            } else {
                env.pushStack(lhs.intValue() + rhs.intValue());
            }
        } else if (lhs.isString() || rhs.isString()) {
            env.pushStack(lhs.stringValue().concat(rhs.stringValue()));
        } else if (lhs.isArray() && rhs.isArray()) {
            Array value = new Array();
            value.setAll(lhs.arrayValue());
            value.setAll(rhs.arrayValue());
            env.pushStack(value);
        } else {
            throw InterpreterError.binaryApplication("+", lhs.type(), rhs.type());
        }
        env.nextPC();
    }
}
