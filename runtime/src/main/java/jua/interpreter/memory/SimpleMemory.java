package jua.interpreter.memory;

import jua.interpreter.address.Address;

public class SimpleMemory implements Memory {

    private final Address[] memory;

    public SimpleMemory(Address[] memory) {
        if (memory == null)
            throw new NullPointerException("Memory is null");
        this.memory = memory;
    }

    @Override
    public Address getAddress(int i) {
        if (i >= size())
            throw new RuntimeException();
        return memory[i];
    }

    @Override
    public Memory subMemory(int offset, int count) {
        if (offset < 0 || count < 0 || (offset + count) >= size()) {
            throw new IndexOutOfBoundsException("size: " + size() + ",offset: " + offset + ", count: " + count);
        }
        return new SegmentMemory(memory, offset, count);
    }

    @Override
    public int size() {
        return memory.length;
    }
}
