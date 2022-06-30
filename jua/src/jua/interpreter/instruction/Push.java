package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.LongOperand;

public final class Push implements Instruction {

    private final LongOperand value;

    public Push(short value) {
        this.value = LongOperand.valueOf(value);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("push");
        printer.print(value);
    }

    @Override
    public int run(InterpreterState state) {
        state.pushStack(value);
        return NEXT;
    }
}
