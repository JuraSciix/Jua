package jua.compiler;

/**
 * Вспомогательный класс, содержащий логику преобразований значений во времени выполнения.
 */
public final class SemanticInfo {

    public enum BoolCode {
        /** Значение эквивалентно {@code true}. */
        TRUE,

        /** Значение эквивалентно {@code false}. */
        FALSE,

        /** Значение нельзя привести к логическому. */
        UNDEFINED;

        public static BoolCode of(boolean value) {
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

        public boolean isUndefined() {
            return this == UNDEFINED;
        }
    }

    public static BoolCode ofBoolean(Object o) {
        if (o == null) return BoolCode.FALSE;
        Class<?> c = o.getClass();
        if (c == Long.class) return BoolCode.of((long) o != 0);
        if (c == Double.class) return BoolCode.of((double) o != 0);
        if (c == Boolean.class) return BoolCode.of((boolean) o);
        if (c == String.class) return BoolCode.of(!((String) o).isEmpty());
        return BoolCode.UNDEFINED;
    }

    private SemanticInfo() {
        throw new AssertionError();
    }
}
