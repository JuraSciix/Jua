package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.interpreter.Trap;
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
        state.setReturnValue(NullOperand.NULL);
        state.setMsg(InterpreterState.MSG_DONE);
        Trap.bti();
        return UNREACHABLE;
    }
}