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
    public int run(InterpreterThread thread) {
        Operand val = thread.popStack();

        if (val.isLong()) {
            thread.pushStack(~val.longValue());
        } else {
            throw InterpreterError.unaryApplication("~", val.type());
        }
        return NEXT;
    }
}