package jua.util;

public final class Conversions {

    public static boolean l2b(long l) {
        return (l & 1) != 0L;
    }

    public static long b2l(boolean b) {
        return b ? 1L : 0L;
    }

    public static String l2s(long l) {
        return Long.toString(l);
    }

    public static String d2s(double d) {
        return Double.toString(d);
    }

    public static int unsigned(short s) { return s & 0xffff; }

    private Conversions() { throw new UnsupportedOperationException(); }
}
