package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.runtime.NullOperand;
import jua.compiler.CodePrinter;

public class PushNull implements Instruction {

    public static final PushNull INSTANCE = new PushNull();

    private PushNull() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("push_null");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack(NullOperand.NULL);
        return NEXT;
    }
}