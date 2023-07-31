package jua.interpreter.memory;

import jua.interpreter.address.Address;

public class SegmentMemory implements Memory {

    private final Address[] memory;

    private final int start;

    private final int end;

    public SegmentMemory(Address[] memory, int start, int end) {
        this.memory = memory;
        this.start = start;
        this.end = end;
    }

    @Override
    public Address getAddress(int i) {
        if (i >= (end - start)) {
            throw new OutOfMemoryError("Segmentation fault");
        }
        return memory[start + i];
    }
}
