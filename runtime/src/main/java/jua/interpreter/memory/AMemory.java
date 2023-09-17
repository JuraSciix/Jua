package jua.interpreter.memory;

public class AMemory implements Memory {

    private final Address[] memory;

    private int offset;
    private int count;

    public AMemory(Address[] memory) {
        if (memory == null)
            throw new NullPointerException("Memory is null");
        this.memory = memory;
        offset = memory.length - 1;
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return count;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public Address getAddress(int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException("Negative address pointer: " + i);
        }
        if (offset < i) {
            throw new IndexOutOfBoundsException("Address pointer exceeds offset: " + offset + ", pointer: " + i);
        }
        return memory[offset - i];
    }

    AMemory a;

    @Override
    public Memory subRegion(int offset, int count) {
        if (a == null)
            a = new AMemory(memory);
        a.offset = this.offset - offset;
        a.count = count;
        return a;
    }

    @Override
    public void freeRegion(int offset, int count) {
        if (offset < 0 || count < 0 || (offset + count) > size())
            throw new IndexOutOfBoundsException("size: " + size() +
                    ", offset: " + offset +
                    ", count: " + count);
        int start = this.offset;
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
