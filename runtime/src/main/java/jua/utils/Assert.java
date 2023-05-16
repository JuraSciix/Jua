package jua.utils;

public final class Assert {

    public static void ensure(boolean cond) {
        assert cond;
    }

    public static void ensure(boolean cond, Object msg) {
        assert cond : msg;
    }

    public static void ensureNonNull(Object obj) {
        assert obj != null;
    }

    public static void ensureNonNull(Object obj, Object msg) {
        assert obj != null : msg;
    }

    public static void error(Object msg) {
        assert false : msg;
    }

    public static void error() {
        assert false;
    }

    private Assert() { error(); }
}
