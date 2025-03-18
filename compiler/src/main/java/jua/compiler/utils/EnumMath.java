package jua.compiler.utils;

public class EnumMath {

    public static <T extends Enum<T>> boolean between(T x, T first, T second) {
        return first.ordinal() <= x.ordinal() && x.ordinal() <= second.ordinal();
    }
}
