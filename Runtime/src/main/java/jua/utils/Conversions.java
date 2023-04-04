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
            int d = unprefixedStr.charAt(i) - '0';
            if (d == 0) continue;
            result += d * Math.pow(radix, dotIdx - i);
        }

        return result;
    }

    public static int unsigned(short s) { return s & 0xffff; }

    private Conversions() { Assert.error(); }
}
