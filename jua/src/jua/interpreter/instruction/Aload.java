package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.NullOperand;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Aload implements Instruction {

    public static final Aload INSTANCE = new Aload();

    @Override
    public int stackAdjustment() { return -1 + -1 + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("aload");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackAload();
        return NEXT;
    }
}