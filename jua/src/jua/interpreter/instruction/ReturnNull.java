package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.NullOperand;

// todo: Переименовать в Leave
public final class ReturnNull implements Instruction {

    public static final ReturnNull INSTANCE = new ReturnNull();

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("return_null");
    }

    @Override
    public int run(InterpreterState state) {
        state.thread().getReturnAddress().setNull();
        state.thread().set_returnee();
        return UNREACHABLE;
    }
}