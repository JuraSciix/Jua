package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.FalseOperand;

public final class ConstFalse implements Instruction {

    public static final ConstFalse CONST_FALSE = new ConstFalse();

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("const_false");
    }

    @Override
    public void run(InterpreterState state) {
        state.constFalse();
    }
}
