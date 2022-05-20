package jua.interpreter.instructions;

import jua.interpreter.InterpreterState;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Astore implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("astore");
    }

    @Override
    public int run(InterpreterState state) {
        Operand val = state.popStack();
        Operand key = state.popStack();
        Operand map = state.popStack();
        map.put(key, val);
        return NEXT;
    }
}