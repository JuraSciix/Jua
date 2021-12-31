package jua.tools;

import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;
import java.util.NoSuchElementException;

public final class ListDequeUtils {

    public static int peekLastInt(IntList list) {
        return list.getInt(lastIndex(list));
    }

    public static int removeLastInt(IntList list) {
        return list.removeInt(lastIndex(list));
    }

    public static void addLastInt(IntList list, int value) {
        list.add(value);
    }

    public static boolean peekLastBoolean(BooleanList list) {
        return list.getBoolean(lastIndex(list));
    }

    public static boolean removeLastBoolean(BooleanList list) {
        return list.removeBoolean(lastIndex(list));
    }

    public static void addLastBoolean(BooleanList list, boolean value) {
        list.add(value);
    }

    public static void setLastBoolean(BooleanList list, boolean newValue) {
        list.set(lastIndex(list), newValue);
    }

    public static int lastIndex(List<?> list) {
        if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.size() - 1;
    }

    private ListDequeUtils() { throw new UnsupportedOperationException(); }
}
