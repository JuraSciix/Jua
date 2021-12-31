package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Dec implements Opcode {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dec");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack(env.popStack().dec());
        return NEXT;
    }
}