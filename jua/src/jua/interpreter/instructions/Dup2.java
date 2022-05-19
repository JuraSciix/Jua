package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Dup2 implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2");
    }

    @Override
    public int run(InterpreterThread thread) {
        Operand a = thread.popStack();
        Operand b = thread.popStack();
        thread.pushStack(b);
        thread.pushStack(a);
        thread.pushStack(b);
        thread.pushStack(a);
        return NEXT;
    }
}