package jua.compiler.utils;

import java.util.function.Supplier;

@Deprecated
public final class Assert {

    // ensure - очень хорошее слово, но check роднее.

    public static void check(boolean cond) {
        assert cond;
    }

    public static void check(boolean cond, Object msg) {
        assert cond : msg;
    }

    public static void check(boolean cond, Supplier<?> messageSupplier) {
        assert cond : messageSupplier.get();
    }

    public static void checkNonNull(Object obj) {
        assert obj != null;
    }

    public static void checkNonNull(Object obj, Object msg) {
        assert obj != null : msg;
    }

    public static void error() {
        assert false;
    }

    public static void error(Object msg) {
        assert false : msg;
    }

    private Assert() { error(); }
}
