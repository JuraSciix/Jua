package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.interpreter.InterpreterError;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Not implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("not");
    }

    @Override
    public int run(InterpreterThread env) {
        Operand val = env.popStack();

        if (val.isLong()) {
            env.pushStack(~val.longValue());
        } else {
            throw InterpreterError.unaryApplication("~", val.type());
        }
        return NEXT;
    }
}