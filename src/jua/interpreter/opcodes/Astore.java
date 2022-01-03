package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.Operand;
import jua.tools.CodePrinter;

public enum Astore implements Opcode {

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