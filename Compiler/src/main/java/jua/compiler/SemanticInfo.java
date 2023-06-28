package jua.compiler;

/**
 * Вспомогательный класс, содержащий логику преобразований значений во времени выполнения.
 */
public final class SemanticInfo {

    public enum BooleanEquivalent {
        /** Значение эквивалентно {@code true}. */
        TRUE,

        /** Значение эквивалентно {@code false}. */
        FALSE,

        /** Значение нельзя привести к логическому. */
        UNDEFINED;

        public static BooleanEquivalent of(boolean value) {
            return value ? TRUE : FALSE;
        }

        public boolean toBoolean() {
            if (this == UNDEFINED) throw new IllegalStateException();
            return isTrue();
        }

        public boolean isTrue() {
            return this == TRUE;
        }

        public boolean isFalse() {
            return this == FALSE;
        }
    }

    public static BooleanEquivalent ofBoolean(Object o) {
        if (o == null) return BooleanEquivalent.FALSE;
        Class<?> c = o.getClass();
        if (c == Long.class) return BooleanEquivalent.of((long) o != 0);
        if (c == Double.class) return BooleanEquivalent.of((double) o != 0);
        if (c == Boolean.class) return BooleanEquivalent.of((boolean) o);
        if (c == String.class) return BooleanEquivalent.of(!((String) o).isEmpty());
        return BooleanEquivalent.UNDEFINED;
    }

    private SemanticInfo() {
        throw new AssertionError();
    }
}
