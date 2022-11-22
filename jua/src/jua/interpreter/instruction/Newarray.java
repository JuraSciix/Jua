package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.ArrayOperand;
import jua.compiler.CodePrinter;

public final class Newarray implements Instruction {

    public static final Newarray INSTANCE = new Newarray();

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("newarray");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackNewArray();
        return NEXT;
    }
}