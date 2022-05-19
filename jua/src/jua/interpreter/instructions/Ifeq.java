package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.compiler.CodePrinter;

public final class Ifeq extends ChainInstruction {

    private final int value;

    public Ifeq(int dest_ip, int value) {
        super(dest_ip);
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifeq");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (env.popInt() == value) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}