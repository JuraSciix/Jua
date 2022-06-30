package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.NullOperand;

public final class ReturnNull implements Instruction {

    public static final ReturnNull INSTANCE = new ReturnNull();

    private ReturnNull() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("return_null");
    }

    @Override
    public int run(InterpreterState state) {
        state.thread().set_return(NullOperand.NULL);
        return UNREACHABLE;
    }
}