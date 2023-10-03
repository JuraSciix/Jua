package jua.runtime.interpreter.memory;

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
    public Memory subRegion(int offset, int count) {
        if (offset < 0 || count < 0 || (offset + count) > size())
            throw new IndexOutOfBoundsException("size: " + size() + ",offset: " + offset + ", count: " + count);

        return new SegmentMemory(memory, offset, count);
    }

    @Override
    public void freeRegion(int offset, int count) {
        if (offset < 0 || count < 0 || (offset + count) > size())
            throw new IndexOutOfBoundsException("size: " + size() +
                    ", offset: " + offset +
                    ", count: " + count);
        int end = offset + count;
        for (int i = offset; i < end; i++) {
            memory[i].reset();
        }
    }

    @Override
    public int size() {
        return memory.length;
    }

    @Override
    public void copyMemory(int srcOffset, Address[] dst, int dstOffset, int count) {
        if (dst == null)
            throw new NullPointerException("Destination memory is null");
        if (srcOffset < 0 || count < 0 || (srcOffset + count) > size())
            throw new IndexOutOfBoundsException("size: " + size() +
                    ", srcOffset: " + srcOffset +
                    ", count: " + count);
        if (dstOffset < 0 || (dstOffset + count) > dst.length)
            throw new IndexOutOfBoundsException("dstLen: " + dst.length +
                    ", dstOffset: " + dstOffset +
                    ", count: " + count);
        System.arraycopy(memory, srcOffset, dst, dstOffset, count);
    }
}
