package jua.vm.simple;

import jua.vm.arena.DataArena;

import java.util.Objects;

import static jua.vm.simple.SimpleType.*;

public class SimpleArena {

    public static void putNull(DataArena arena, int addr) {
        arena.writeType(addr, TYPE_PTR64);
        arena.writePtr64(addr, -1);
    }

    public static void putInt64(DataArena arena, int addr, long value) {
        arena.writeType(addr, TYPE_INT64);
        arena.writeInt64(addr, value);
    }

    public static void putFloat64(DataArena arena, int addr, double value) {
        arena.writeType(addr, TYPE_FLOAT64);
        arena.writeFloat64(addr, value);
    }

    public static void putBool(DataArena arena, int addr, boolean value) {
        arena.writeType(addr, TYPE_BOOL64);
        arena.writeBool(addr, value);
    }

    public static String toString(DataArena arena, int addr) {
        byte type = arena.readType(addr);
        switch (type) {
            case TYPE_INT64: return Long.toString(arena.readInt64(addr));
            case TYPE_FLOAT64: return Double.toString(arena.readFloat64(addr));
            case TYPE_BOOL64: return Boolean.toString(arena.readBool(addr));
            case TYPE_PTR64: return Objects.toString(arena.readBool(addr));
            default: return String.format("%02x:%016x", type, arena.readInt64(addr));
        }
    }
}
