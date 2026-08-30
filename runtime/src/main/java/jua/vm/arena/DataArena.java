package jua.vm.arena;

import java.util.Arrays;

import static jua.vm.simple.SimpleType.TYPE_INVALID;

public final class DataArena {
    private static final int INITIAL_CAPACITY = 512;

    private byte[] typeLine = new byte[INITIAL_CAPACITY];
    private long[] valueLine = new long[INITIAL_CAPACITY];
    private int allocated = 0;

    public void allocate(int count) {
        int capacity = typeLine.length;
        if (count > capacity - allocated) {
            int newLength = (allocated + count) * 2;
            typeLine = Arrays.copyOf(typeLine, newLength);
            valueLine = Arrays.copyOf(valueLine, newLength);
        }
        allocated += count;
    }

    public void deallocate(int count) {
        if (count > allocated) {
            throw new IllegalArgumentException("Deallocation count is greater than allocated count");
        }
        Arrays.fill(typeLine, allocated - 1 - count, allocated, TYPE_INVALID);
        allocated -= count;
    }

    public void move(int src, int dst) {
        typeLine[dst] = typeLine[src];
        valueLine[dst] = valueLine[src];
    }

    public byte readType(int addr) {
        return typeLine[addr];
    }

    public long readInt64(int addr) {
        return valueLine[addr];
    }

    public double readFloat64(int addr) {
        return Double.longBitsToDouble(valueLine[addr]);
    }

    public boolean readBool(int addr) {
        return valueLine[addr] != 0;
    }

    public int readPtr64(int addr) {
        return (int) valueLine[addr];
    }

    public void writeType(int addr, byte type) {
        typeLine[addr] = type;
    }

    public void writeInt64(int addr, long val) {
        valueLine[addr] = val;
    }

    public void writeFloat64(int addr, double val) {
        valueLine[addr] = Double.doubleToRawLongBits(val);
    }

    public void writeBool(int addr, boolean val) {
        valueLine[addr] = val ? 1L : 0L;
    }

    public void writePtr64(int addr, int val) {
        valueLine[addr] = val;
    }
}
