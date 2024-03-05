package jua.runtime.memory;

import jua.runtime.heap.ListHeap;
import jua.runtime.heap.StringHeap;

import static jua.runtime.Types.*;

public class Memories { // *mourning the memories*

    public static int typeUnion(Memory m, int lhs, int rhs) {
        return getTypeUnion(m.getTypeAt(lhs), m.getTypeAt(rhs));
    }

    public static void setLongType(Memory m, int address, long value) {
        m.setTypeAt(address, T_INT);
        m.setLongAt(address, value);
    }

    public static void setDoubleType(Memory m, int address, double value) {
        m.setTypeAt(address, T_FLOAT);
        m.setDoubleAt(address, value);
    }

    public static void setBooleanType(Memory m, int address, boolean value) {
        m.setTypeAt(address, T_BOOLEAN);
        m.setLongAt(address, value ? 1 : 0);
    }

    public static void setStringType(Memory m, int address, StringHeap value) {
        m.setTypeAt(address, T_STRING);
        m.setRefAt(address, value);
    }

    public static StringHeap getStringHeap(Memory m, int address) {
        return (StringHeap) m.getRefAt(address);
    }

    public static void setListType(Memory m, int address, ListHeap value) {
        m.setTypeAt(address, T_LIST);
        m.setRefAt(address, value);
    }

    public static ListHeap getListHeap(Memory m, int address) {
        return (ListHeap) m.getRefAt(address);
    }
}
