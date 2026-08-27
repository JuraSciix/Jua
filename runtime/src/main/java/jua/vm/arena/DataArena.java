package jua.vm.arena;

import java.util.Arrays;

import static jua.vm.SimpleType.TYPE_INVALID;
import static jua.vm.SimpleType.TYPE_REF;

public final class DataArena {
    private static final int INITIAL_CAPACITY = 512;

    private static final int HANDLE_NULL = -1;

    private final ReferenceArena referenceArena = new ReferenceArena();
    private byte[] typeLine = new byte[INITIAL_CAPACITY];
    private long[] valueLine = new long[INITIAL_CAPACITY];
    private int allocated = 0;

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

    public Object readReference(int addr) {
        int handle = handle(addr);
        return handle != HANDLE_NULL ? referenceArena.read(handle) : null;
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

    public void writeReference(int addr, Object val) {
        valueLine[addr] = val != null ? referenceArena.put(val) : HANDLE_NULL;
    }

    public void clearAndMove(int src, int dst) {
        clear(dst);
        move(src, dst);
    }

    // Переносит значение из одного адреса в другой.
    // ВНИМАНИЕ!!!
    // Метод не занимается очисткой ссылок.
    // Это необходимо делать вручную!
    public void move(int src, int dst) {
        typeLine[dst] = typeLine[src];
        valueLine[dst] = valueLine[src];
    }

    public void clear(int addr) {
        int handle;
        if (typeLine[addr] == TYPE_REF && (handle = handle(addr)) != HANDLE_NULL)
            referenceArena.delete(handle);
        typeLine[addr] = TYPE_INVALID;
    }

    private int handle(int addr) {
        return (int) valueLine[addr];
    }

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
        // Очищаем ссылки и типы.
        for (int i = 0; i < count; i++) {
            int addr = allocated - 1 - i;
            int handle;
            if (typeLine[addr] == TYPE_REF && (handle = handle(addr)) != HANDLE_NULL)
                referenceArena.delete(handle);
            typeLine[addr] = TYPE_INVALID;
        }
        allocated -= count;
    }
}
