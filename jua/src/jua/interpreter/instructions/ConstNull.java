package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterRuntime;
import jua.runtime.NullOperand;

public final class ConstNull implements Instruction {

    public static final ConstNull INSTANCE = new ConstNull();

    private ConstNull() { super(); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("const_null");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack(NullOperand.NULL);
        return NEXT;
    }
}