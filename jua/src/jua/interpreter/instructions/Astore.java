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
    public int run(InterpreterThread thread) {
        Operand val = thread.popStack();
        Operand key = thread.popStack();
        Operand map = thread.popStack();
        map.put(key, val);
        return NEXT;
    }
}