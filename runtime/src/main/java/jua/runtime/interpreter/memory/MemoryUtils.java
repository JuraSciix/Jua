package jua.runtime.interpreter.memory;

public class MemoryUtils {

    /**
     * Проверяет, чтобы значение находилось в интервале [A; B).
     *
     * @param i Значение.
     * @param lowIncl Начало интервала (A).
     * @param highExcl Конец интервала (B).
     * @throws OutOfMemoryError если значение не находится в интервале.
     */
    public static void assumeBounds(int i, int lowIncl, int highExcl) {
        if (i < lowIncl || i >= highExcl) {
            throw new OutOfMemoryError(i + " is out of range: [" + lowIncl + "; " + highExcl + ")");
        }
    }
}
