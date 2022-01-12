package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Aload implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("aload");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand key = env.popStack();
        env.pushStack(env.popArray().get(key));
        return NEXT;
    }
}