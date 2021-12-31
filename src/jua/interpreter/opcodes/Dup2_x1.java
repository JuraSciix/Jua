package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Dup2_x1 implements Opcode {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup2_x1");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.getFrame().dup2_x2();
        return NEXT;
    }
}