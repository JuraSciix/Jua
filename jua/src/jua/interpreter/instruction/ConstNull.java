package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.NullOperand;

public final class ConstNull implements Instruction {

    public static final ConstNull INSTANCE = new ConstNull();

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("const_null");
    }

    @Override
    public void run(InterpreterState state) {
        state.constNull();
    }
}