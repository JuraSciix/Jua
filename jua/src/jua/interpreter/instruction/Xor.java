package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Xor implements Instruction {

    public static final Xor INSTANCE = new Xor();

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("xor");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackXor();
        return NEXT;
    }
}