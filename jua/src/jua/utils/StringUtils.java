package jua.utils;

import java.util.function.IntPredicate;


public class StringUtils {

    /**
     * Удаляет из строки все whitespace-символы, и если строка стала пустой, возвращает {@code null}, или ту же строку.
     */
    public static String stripWhitespacesToNull(String str) {
        String strippedStr = stripCodePoints(str, Character::isWhitespace);
        if (strippedStr.isEmpty()) {
            return null;
        }
        return strippedStr;
    }

    /**
     * Удаляет все символы строки по указанному предикату и возвращает результат.
     */
    public static String stripCodePoints(String str, IntPredicate filter) {
        // Функциональный вариант для Java 9+
//        return str.codePoints()
//                .filter(jua.utils.Predicates.negate(Character::isWhitespace))
//                .boxed()
//                .collect(jua.utils.MoreCollectors.codePointCollector())
//                .toString();

        int pos = 0;
        int len = str.length();
        StringBuilder buffer = new StringBuilder(len);
        while (pos < len) {
            int cp = str.codePointAt(pos);

            if (filter.test(cp)) {
                buffer.appendCodePoint(cp);
            }
            pos += Character.charCount(cp);
        }

        if (buffer.length() == len) {
            // Если размер буфера соответствует строке, значит ни один символ не был удален
            // И сохранять в памяти отдельную, идентичную строку не нужно.
            return str;
        }

        return buffer.toString();
    }

    public static boolean isBlank(String str) {
        return (str == null) || str.isEmpty();
    }

    public static boolean nonBlank(String str) {
        return !isBlank(str);
    }

    private StringUtils() { Assert.error(); }
}
