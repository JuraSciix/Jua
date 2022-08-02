package jua.util;

public class Conversions {

    public static boolean j2z(long j) {
        return (j & 1) != 0L;
    }

    public static long z2j(boolean z) {
        return z ? 1L : 0L;
    }
}
