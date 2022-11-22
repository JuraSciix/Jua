package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Shl implements Instruction {

    public static final Shl INSTANCE = new Shl();

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("shl");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackShl();
        return NEXT;
    }
}