package jua.interpreter.instructions;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.DoubleOperand;
import jua.runtime.LongOperand;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Neg implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("neg");
    }

    @Override
    public int run(InterpreterState state) {
        Operand val = state.popStack();

        if (val.isLong()) {
            state.pushStack(new LongOperand(-val.longValue()));
        } else if (val.isDouble()) {
            state.pushStack(new DoubleOperand(-val.doubleValue()));
        } else {
            throw InterpreterError.unaryApplication("-", val.type());
        }
        return NEXT;
    }
}