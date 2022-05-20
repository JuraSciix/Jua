package jua.interpreter.instructions;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.LongOperand;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Shl implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("shl");
    }

    @Override
    public int run(InterpreterState state) {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (lhs.isLong() && rhs.isLong()) {
            state.pushStack(new LongOperand(lhs.longValue() << rhs.longValue()));
        } else {
            throw InterpreterError.binaryApplication("<<", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}