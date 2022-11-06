package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Sub implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("sub");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackSub();
        return NEXT;
    }
}