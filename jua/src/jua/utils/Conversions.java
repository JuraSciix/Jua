package jua.utils;

public final class Conversions {

    public static long parseLong(String str, int radix) {
        String unprefixedStr;
        switch (radix) {
            case 8:  // oct
            case 10: // dec
                unprefixedStr = str;
                break;
            case 2:  // bin
            case 12: // duo
            case 16: // hex
                unprefixedStr = str.substring(2); // "0b", "0B", "0d", "0D", "0x", "0X"
                break;
            default:
                throw new IllegalArgumentException("Unsupported radix: " + radix);
        }
        return Long.parseLong(unprefixedStr, radix);
    }

    public static double parseDouble(String str, int radix) {
        String unprefixedStr;
        switch (radix) {
            case 10: // dec
            case 16: // hex
                return Double.parseDouble(str);
            case 2:  // bin
            case 12: // duo
                unprefixedStr = str.substring(2); // "0b", "0B", "0d", "0D"
                break;
            case 8:  // oct
                unprefixedStr = str;
                break;
            default:
                throw new IllegalArgumentException("Unsupported radix: " + radix);
        }

        int dotIdx = unprefixedStr.indexOf('.');

        if (dotIdx < 0) // integer...
            return parseLong(str, radix);

        double result = 0.0;
        for (int i = 0; i < unprefixedStr.length(); i++) {
            if (i == dotIdx) continue;
            result += (unprefixedStr.charAt(i) - '0') * Math.pow(radix, dotIdx - i);
        }

        return result;
    }

    public static boolean l2b(long l) {
        return (l & 1) != 0L;
    }

    public static boolean d2b(double d) { return (d != 0.0); }

    public static long b2l(boolean b) {
        return b ? 1L : 0L;
    }

    public static double b2d(boolean b) {
        return b ? 1.0 : 0.0;
    }

    public static String l2s(long l) {
        return Long.toString(l);
    }

    public static String d2s(double d) {
        return Double.toString(d);
    }

    public static int unsigned(short s) { return s & 0xffff; }

    private Conversions() { Assert.error(); }
}
