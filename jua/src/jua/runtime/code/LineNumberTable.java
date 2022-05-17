package jua.runtime.code;

public final class LineNumberTable {

    private final short[] codePoints;

    private final int[] lineNumbers;

    public LineNumberTable(short[] codePoints, int[] lineNumbers) {
        this.lineNumbers = lineNumbers.clone();
        this.codePoints = codePoints.clone();
    }

    public int lineNumberOf(int codePoint) {
        short[] _codePoints = codePoints;

        int f = 0;                  // from
        int t = _codePoints.length; // to
        int c = (t >> 1);           // center

        while ((t - f) > 1) {
            int cp = _codePoints[c] & 0xffff;
            if (codePoint >= cp) {
                f = c;
            } else {
                t = c;
            }
            c = (t + f) >> 1;
        }

        return lineNumbers[c];
    }
}
