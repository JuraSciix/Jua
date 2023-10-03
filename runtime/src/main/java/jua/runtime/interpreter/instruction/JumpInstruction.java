package jua.runtime.interpreter.instruction;

public abstract class JumpInstruction implements Instruction {

    private final int nextCp;

    protected JumpInstruction(int nextCp) {
        this.nextCp = nextCp;
    }

    public int getNextCp() {
        return nextCp;
    }
}
