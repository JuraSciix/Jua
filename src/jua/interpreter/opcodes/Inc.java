package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Inc implements Opcode {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("inc");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack(env.popStack().inc());
        return NEXT;
    }
}