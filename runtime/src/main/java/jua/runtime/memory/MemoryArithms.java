package jua.runtime.memory;

import jua.runtime.heap.ListHeap;
import jua.runtime.heap.StringHeap;

import static jua.runtime.Types.*;
import static jua.runtime.memory.Memories.*;

public class MemoryArithms {

    private static final int INT_INT = getTypeUnion(T_INT, T_INT);
    private static final int FLOAT_FLOAT = getTypeUnion(T_FLOAT, T_FLOAT);

    public static boolean add(Memory m, int lhs, int rhs, int out) {
        int u = typeUnion(m, lhs, rhs);

        if (INT_INT == u) {
            setLongType(m, out, m.getLongAt(lhs) + m.getLongAt(rhs));
            return true;
        }

        if (FLOAT_FLOAT == u) {
            setDoubleType(m, out, m.getDoubleAt(lhs) + m.getDoubleAt(rhs));
            return true;
        }

        if (getTypeUnion(T_INT, T_FLOAT) == u) {
            setDoubleType(m, out, m.getLongAt(lhs) + m.getDoubleAt(rhs));
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_INT) == u) {
            setDoubleType(m, out, m.getDoubleAt(lhs) + m.getLongAt(rhs));
            return true;
        }

        if (getTypeUnion(T_STRING, T_STRING) == u) {
            StringHeap s = getStringHeap(m, lhs);
            s.append(getStringHeap(m, rhs));
            setStringType(m, out, s);
            return true;
        }

        return false;
    }

    public static int compare(Memory m, int lhs, int rhs, int unexpected) {
        int u = typeUnion(m, lhs, rhs);

        if (INT_INT == u) {
            return Long.compare(m.getLongAt(lhs), m.getLongAt(rhs));
        }

        if (FLOAT_FLOAT == u) {
            double x = m.getDoubleAt(lhs);
            double y = m.getDoubleAt(rhs);
            if (Double.isNaN(x) || Double.isNaN(y)) {
                return unexpected;
            } else {
                return Double.compare(x, y);
            }
        }

        if (getTypeUnion(T_INT, T_FLOAT) == u) {
            return Double.compare(m.getLongAt(lhs), m.getDoubleAt(rhs));
        }

        if (getTypeUnion(T_FLOAT, T_INT) == u) {
            return Double.compare(m.getDoubleAt(rhs), m.getLongAt(lhs));
        }

        if (getTypeUnion(T_STRING, T_STRING) == u) {
            StringHeap x = getStringHeap(m, lhs);
            StringHeap y = getStringHeap(m, rhs);
            return x.fastCompareWith(y);
        }

        if (getTypeUnion(T_LIST, T_LIST) == u) {
            ListHeap x = getListHeap(m, lhs);
            ListHeap y = getListHeap(m, rhs);
            return x.fastCompare(y, unexpected);
        }

        if (getTypeUnion(T_NULL, T_NULL) == u) {
            return 0;
        }

        return unexpected;
    }

    public static boolean inc(Memory m, int address) {
        if (m.getTypeAt(address) == T_INT) {
            m.setLongAt(address, m.getLongAt(address) + 1);
            return true;
        }
        if (m.getTypeAt(address) == T_FLOAT) {
            m.setDoubleAt(address, m.getDoubleAt(address) + 1);
            return true;
        }

        return false;
    }
}
