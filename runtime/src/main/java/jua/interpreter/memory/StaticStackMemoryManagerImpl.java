package jua.interpreter.memory;

import jua.interpreter.address.AddressUtils;

public class StaticStackMemoryManagerImpl implements StackMemoryManager {

    private final Memory memory;

    private int top = 0;

    public StaticStackMemoryManagerImpl(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be greater than zero");
        }
        memory = new SimpleMemory(AddressUtils.allocateMemory(size, 0));
    }

    @Override
    public Memory allocate(int count) {
        Memory region = memory.subRegion(top, count);
        top += count;
        return region;
    }

    @Override
    public void free(int count) {
        memory.freeRegion(top -= count, count);
    }
}
