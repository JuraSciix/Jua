package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.Operand;
import jua.tools.CodePrinter;

public enum Aload implements Opcode {

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