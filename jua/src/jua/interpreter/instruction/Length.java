package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.LongOperand;

public enum Length implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.print("length");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackLength();
        return NEXT;
    }
}
