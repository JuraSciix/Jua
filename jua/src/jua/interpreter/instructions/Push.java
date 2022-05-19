package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterRuntime;
import jua.runtime.LongOperand;

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
    public int run(InterpreterRuntime env) {
        env.pushStack(value);
        return NEXT;
    }
}
