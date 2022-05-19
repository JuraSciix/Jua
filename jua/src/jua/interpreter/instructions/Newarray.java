package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.runtime.ArrayOperand;
import jua.compiler.CodePrinter;

public enum Newarray implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("newarray");
    }

    @Override
    public int run(InterpreterThread thread) {
        thread.pushStack(new ArrayOperand());
        return NEXT;
    }
}