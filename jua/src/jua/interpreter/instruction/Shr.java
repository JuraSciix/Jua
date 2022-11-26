package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Shr implements Instruction {

    public static final Shr INSTANCE = new Shr();

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("shr");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackShr();
    }
}