package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Shr implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("shr");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackShr();
        return NEXT;
    }
}