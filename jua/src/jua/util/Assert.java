package jua.util;

public class Assert {

    public static void check(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    public static void check(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    public static void notNull(Object obj) {
        if (obj == null) {
            throw new AssertionError();
        }
    }

    public static void notNull(Object obj, String msg) {
        if (obj == null) {
            throw new AssertionError(msg);
        }
    }

    public static void error() {
        throw new AssertionError();
    }

    public static void error(Object o) {
        throw new AssertionError(o);
    }
}
