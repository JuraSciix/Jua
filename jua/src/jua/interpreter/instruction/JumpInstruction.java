package jua.interpreter.instruction;

import jua.compiler.CodePrinter;

public abstract class JumpInstruction implements Instruction {

    protected final int destIp;

    protected JumpInstruction(int destIp) {
        this.destIp = destIp;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printIp(destIp);
    }
}