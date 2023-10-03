package jua.runtime.interpreter.memory;

public class SegmentMemory implements Memory {

    private final Address[] memory;
    private final int offset;
    private final int count;

    public SegmentMemory(Address[] memory, int offset, int count) {
        if (memory == null)
            throw new NullPointerException("Memory is null");
        if (offset < 0 || count < 0 || (offset + count) > memory.length)
            throw new IndexOutOfBoundsException("size: " + memory.length +
                    ", offset: " + offset +
                    ", count: " + count);
        this.memory = memory;
        this.offset = offset;
        this.count = count;
    }

    private static void checkBounds(int size, int offset, int count) {
        if (offset < 0 || count < 0 || (offset + count) > size)
            throw new IndexOutOfBoundsException("size: " + size +
                    ", offset: " + offset +
                    ", count: " + count);
    }

    @Override
    public Address getAddress(int i) {
        if (i < 0 || i >= size())
            throw new IndexOutOfBoundsException("size: " + size() + ", i: " + i);
        return memory[offset + i];
    }

    @Override
    public Memory subRegion(int offset, int count) {
        // Проверки на применимость offset и count уже находятся в конструкторе.
        return new SegmentMemory(memory, this.offset + offset, count);
    }

    @Override
    public void freeRegion(int offset, int count) {
        if (offset < 0 || count < 0 || (offset + count) > size())
            throw new IndexOutOfBoundsException("size: " + size() +
                    ", offset: " + offset +
                    ", count: " + count);
        int start = this.offset + offset;
        int end = start + count;
        for (int i = start; i < end; i++) {
            memory[i].reset();
        }
    }

    @Override
    public int size() {
        return count;
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
            throw new IndexOutOfBoundsException("dstLength: " + dst.length +
                    ", dstOffset: " + dstOffset +
                    ", count: " + count);
        System.arraycopy(memory, offset + srcOffset, dst, dstOffset, count);
    }
}
