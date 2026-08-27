package jua.vm;

import jua.vm.arena.DataArena;

import java.util.Objects;

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

    public static String toString(DataArena arena, int addr) {
        byte type = arena.readType(addr);
        switch (type) {
            case TYPE_INT64: return Long.toString(arena.readInt64(addr));
            case TYPE_FLOAT64: return Double.toString(arena.readFloat64(addr));
            case TYPE_BOOL64: return Boolean.toString(arena.readBool(addr));
            case TYPE_REF: return Objects.toString(arena.readBool(addr));
            default: return String.format("%02x:%016x", type, arena.readInt64(addr));
        }
    }
}
