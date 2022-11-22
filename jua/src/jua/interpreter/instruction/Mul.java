package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Mul implements Instruction {

    public static final Mul INSTANCE = new Mul();

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

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