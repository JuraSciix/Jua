package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Astore implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("astore");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand val = env.popStack();
        Operand key = env.popStack();
        env.popArray().set(key, val);
        return NEXT;
    }
}