package jua.interpreter.instruction;

import jua.compiler.CodePrinter;

public abstract class JumpInstruction implements Instruction {

    public int offset;

    protected JumpInstruction() {
        super();
    }

    /** @deprecated Для временной совместимости. */
    @Deprecated
    protected JumpInstruction(int offset) {
        this.offset = offset;
    }

    @Override
    public abstract JumpInstruction negate();

    @Override
    public void setOffset(int pc) {
        offset = pc;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printIp(offset);
    }
}