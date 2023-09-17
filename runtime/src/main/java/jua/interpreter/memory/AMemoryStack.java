package jua.interpreter.memory;

public class AMemoryStack implements MemoryStack {

    private final AMemory memory;

    public AMemoryStack(int capacity) {
        this.memory = new AMemory(AddressUtils.allocateMemory(capacity, 0));
    }

    @Override
    public Memory allocate(int count) {
        memory.setOffset(memory.getOffset() - count);
        return memory;
    }

    @Override
    public void free(int count) {
        memory.setOffset(memory.getOffset() + count);
    }
}
