package jua.utils;

public final class Assert {

    public static <T> T notNull(T value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        return value;
    }

    public static <T> T notNull(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return value;
    }

    public static void ensure(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    public static void ensure(boolean cond, Object msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    public static void ensureNonNull(Object obj) {
        if (obj == null) {
            throw new AssertionError();
        }
    }

    public static void ensureNonNull(Object obj, Object msg) {
        if (obj == null) {
            throw new AssertionError(msg);
        }
    }

    public static void error(Object msg) {
        throw new AssertionError(msg);
    }

    public static void error() {
        throw new AssertionError();
    }

    private Assert() { error(); }
}
