package jua.interpreter.instructions;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.DoubleOperand;
import jua.runtime.heap.LongOperand;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Rem implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("rem");
    }

    @Override
    public int run(InterpreterState state) {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        lhs.rem(rhs);
        return NEXT;
    }
}