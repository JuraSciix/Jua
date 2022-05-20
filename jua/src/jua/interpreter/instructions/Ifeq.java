package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

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
    public int run(InterpreterState state) {
        if (state.popInt() == value) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}