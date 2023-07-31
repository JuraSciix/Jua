package jua.interpreter.memory;

import jua.interpreter.address.Address;

public class SegmentMemory implements Memory {

    private final Address[] memory;
    private final int offset;
    private final int count;

    public SegmentMemory(Address[] memory, int offset, int count) {
        if (memory == null)
            throw new NullPointerException("Memory is null");
        if (offset < 0 || count < 0 || (offset + count) > memory.length)
            throw new IndexOutOfBoundsException("size: " + memory.length + ", offset: " + offset + ", count: " + count);
        this.memory = memory;
        this.offset = offset;
        this.count = count;
    }

    @Override
    public Address getAddress(int i) {
        if (i >= count)
            throw new RuntimeException();
        return memory[offset + i];
    }

    @Override
    public Memory subMemory(int offset, int count) {
        // Проверки на применимость offset и count уже находятся в конструкторе.
        return new SegmentMemory(memory, this.offset + offset, count);
    }

    @Override
    public int size() {
        return count;
    }
}
