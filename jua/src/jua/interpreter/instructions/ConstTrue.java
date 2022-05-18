package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterThread;
import jua.runtime.TrueOperand;

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
    public int run(InterpreterThread env) {
        env.pushStack(TrueOperand.TRUE);
        return NEXT;
    }
}
