package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class And implements Instruction {

    public static final And INSTANCE = new And();

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("and");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackAnd();
        return NEXT;
    }
}