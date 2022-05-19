package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.runtime.ArrayOperand;
import jua.compiler.CodePrinter;

public enum Newarray implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("newarray");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack(new ArrayOperand());
        return NEXT;
    }
}