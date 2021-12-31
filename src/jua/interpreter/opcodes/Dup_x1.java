package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Dup_x1 implements Opcode {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup_x1");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.getFrame().dup1_x1();
        return NEXT;
    }
}