package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Xor implements Instruction {

    INSTANCE;

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