package jua.runtime;

public final class Operations {

    /*
     * Ниже встречаются следующие выражения:
     *  f() > RESULT_FALSE — функция приняла RESULT_TRUE.
     *  f() == RESULT_FALSE — функция приняла RESULT_FALSE.
     *  f() < RESULT_FALSE — возникла ошибка (RESULT_FAILURE).
     *
     * Я использую порядковые сравнения вместо строгих для минимизации
     * количества инструкций в байт-коде.
     */

    /** Операция вернула {@code false}. */
    public static final int RESULT_FALSE = 0;

    /** Операция вернула {@code true}. */
    public static final int RESULT_TRUE = 1;

    /** Не удалось завершить операцию из-за ошибки. */
    public static final int RESULT_FAILURE = -1;

    public static boolean isResultTrue(int resultCode) {
        return resultCode > RESULT_FALSE;
    }

    public static boolean isResultFalse(int resultCode) {
        return resultCode == RESULT_FALSE;
    }

    public static boolean isResultFailure(int resultCode) {
        return resultCode < RESULT_FALSE;
    }

    public static int toResultCode(boolean value) {
        return value ? RESULT_TRUE : RESULT_FALSE;
    }

    public static boolean fromResultCode(int resultCode) {
        if (isResultFailure(resultCode)) {
            throw new RuntimeException("Failure");
        }
        return isResultTrue(resultCode);
    }
}
