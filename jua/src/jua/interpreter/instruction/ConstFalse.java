package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.FalseOperand;

public final class ConstFalse implements Instruction {

    public static final ConstFalse CONST_FALSE = new ConstFalse();

    private ConstFalse() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("const_false");
    }

    @Override
    public int run(InterpreterState state) {
        state.constFalse();
        return NEXT;
    }
}
