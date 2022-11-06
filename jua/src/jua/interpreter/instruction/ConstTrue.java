package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class ConstTrue implements Instruction {

    public static final ConstTrue CONST_TRUE = new ConstTrue();

    private ConstTrue() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("const_true");
    }

    @Override
    public int run(InterpreterState state) {
        state.constTrue();
        return NEXT;
    }
}
