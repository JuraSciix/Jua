package jua.util;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Assertions {

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

    public static <T> T notNull(T value, Supplier<String> nameSupplier) {
        if (value == null) {
            throw new IllegalArgumentException(nameSupplier.get() + " must not be null");
        }
        return value;
    }

    public static <T> T validate(T value, Predicate<? super T> validator) {
        if (!validator.test(value)) {
            throw new IllegalArgumentException();
        }
        return value;
    }

    public static <T> T validate(T value, Predicate<? super T> validator, String msg) {
        if (!validator.test(value)) {
            throw new IllegalArgumentException(msg);
        }
        return value;
    }

    public static <T> T validate(T value, Predicate<? super T> validator, Supplier<String> msg) {
        if (!validator.test(value)) {
            throw new IllegalArgumentException(msg.get());
        }
        return value;
    }

    public static void require(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    public static void require(boolean cond, Object msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    public static void require(boolean cond, Supplier<?> msgSupplier) {
        if (!cond) {
            throw new AssertionError(msgSupplier.get());
        }
    }

    public static void error(Object msg) {
        throw new AssertionError(msg);
    }

    public static void error() {
        throw new AssertionError();
    }

    private Assertions() { error(); }
}
