package jua.interpreter.instruction;

import jua.compiler.CodePrinter;

public abstract class ChainInstruction implements Instruction {

    protected final int destIp;

    protected ChainInstruction(int destIp) {
        this.destIp = destIp;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printIp(destIp);
    }
}