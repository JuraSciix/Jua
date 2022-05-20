package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.LongOperand;

public enum Length implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.print("length");
    }

    @Override
    public int run(InterpreterState state) {
        state.pushStack(new LongOperand(state.popStack().length()));
        return NEXT;
    }
}
