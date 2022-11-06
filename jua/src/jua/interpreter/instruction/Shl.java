package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Shl implements Instruction {

    INSTANCE;

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