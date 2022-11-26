package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Rem implements Instruction {

    public static final Rem INSTANCE = new Rem();

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("rem");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackRem();
    }
}