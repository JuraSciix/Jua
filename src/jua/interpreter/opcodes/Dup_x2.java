package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public enum Dup_x2 implements Opcode {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("dup_x2");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.getFrame().dup1_x2();
        return NEXT;
    }
}