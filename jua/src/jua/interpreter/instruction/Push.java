package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.LongOperand;

// todo: Переименовать инструкцию в iconst
public final class Push implements Instruction {

    private final short value;

    public Push(short value) {
        this.value = value;
    }

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("push");
        printer.print(value);
    }

    @Override
    public void run(InterpreterState state) {
        state.push(value);
    }
}
