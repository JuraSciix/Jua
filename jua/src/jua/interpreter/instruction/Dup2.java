package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Dup2 implements Instruction {

    public static final Dup2 INSTANCE = new Dup2();

    @Override
    public int stackAdjustment() {
        return -1 + -1 + 1 + 1 + 1 + 1;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2");
    }

    @Override
    public void run(InterpreterState state) {
        state.dup2();
    }
}