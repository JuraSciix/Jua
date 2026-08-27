package jua.vm;

import jua.vm.arena.DataArena;

import static jua.vm.SimpleType.*;

public class SimpleArena {

    public static void putNull(DataArena arena, int addr) {
        arena.writeType(addr, TYPE_REF);
        arena.writeReference(addr, null);
    }

    public static void putBool(DataArena arena, int addr, boolean value) {
        arena.writeType(addr, TYPE_BOOL64);
        arena.writeBool(addr, value);
    }

    public static void putInt64(DataArena arena, int addr, long value) {
        arena.writeType(addr, TYPE_INT64);
        arena.writeInt64(addr, value);
    }

    public static void clearAndMove(DataArena arena, int src, int dst) {
        arena.clear(dst);
        arena.move(src, dst);
    }
}
