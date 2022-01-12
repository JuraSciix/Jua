package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.interpreter.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Neg implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("neg");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand val = env.popStack();

        if (val.isInt()) {
            env.pushStack(-val.intValue());
        } else if (val.isFloat()) {
            env.pushStack(-val.floatValue());
        } else {
            throw InterpreterError.unaryApplication("-", val.type());
        }
        return NEXT;
    }
}