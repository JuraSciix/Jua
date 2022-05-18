package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Astore implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("astore");
    }

    @Override
    public int run(InterpreterThread env) {
        Operand val = env.popStack();
        Operand key = env.popStack();
        Operand map = env.popStack();
        map.put(key, val);
        return NEXT;
    }
}