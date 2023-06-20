package jua.compiler;

/**
 * Вспомогательный класс, содержащий логику преобразований значений во времени выполнения.
 */
public final class SemanticInfo {

    public enum Bool {
        /** Значение эквивалентно {@code true}. */
        TRUE,

        /** Значение эквивалентно {@code false}. */
        FALSE,

        /** Значение нельзя привести к логическому. */
        UNDEFINED;

        public static Bool of(boolean value) {
            return value ? TRUE : FALSE;
        }
    }

    public static Bool convertToBoolean(Object o) {
        if (o == null) return Bool.FALSE;
        Class<?> c = o.getClass();
        if (c == Long.class) return Bool.of((long) o != 0);
        if (c == Double.class) return Bool.of((double) o != 0);
        if (c == Boolean.class) return Bool.of((boolean) o);
        if (c == String.class) return Bool.of(!((String) o).isEmpty());
        return Bool.UNDEFINED;
    }

    private SemanticInfo() {
        throw new AssertionError();
    }
}
