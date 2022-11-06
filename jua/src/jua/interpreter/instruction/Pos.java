package jua.interpreter.instruction;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public enum Pos implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pos");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackPos();
        return NEXT;
    }
}