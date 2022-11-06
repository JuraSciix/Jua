package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Mul implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("mul");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackMul();
        return NEXT;
    }
}