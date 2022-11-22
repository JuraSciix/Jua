package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.LongOperand;

public final class Length implements Instruction {

    public static final Length INSTANCE = new Length();

    @Override
    public int stackAdjustment() { return -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.print("length");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackLength();
        return NEXT;
    }
}
