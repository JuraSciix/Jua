package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.runtime.Operand;
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

        if (val.isLong()) {
            env.pushStack(-val.longValue());
        } else if (val.isDouble()) {
            env.pushStack(-val.doubleValue());
        } else {
            throw InterpreterError.unaryApplication("-", val.type());
        }
        return NEXT;
    }
}