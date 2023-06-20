package jua.interpreter.instruction;

public abstract class JumpInstruction implements Instruction {

    protected int offsetJump;

    @Override
    public abstract JumpInstruction negated();

    @Override
    public JumpInstruction offsetJump(int offsetJump) {
        this.offsetJump = offsetJump;
        return this;
    }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printOffsetJump(offsetJump);
        printer.restoreTosIn(offsetJump);
    }
}