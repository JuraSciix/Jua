package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.Operand;
import jua.tools.CodePrinter;

public enum Dup2 implements Opcode {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand a = env.popStack();
        Operand b = env.popStack();
        env.pushStack(b);
        env.pushStack(a);
        env.pushStack(b);
        env.pushStack(a);
        return NEXT;
    }
}