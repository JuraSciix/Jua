package jua.runtime.code;

import jua.runtime.utils.Conversions;

public final class LineNumberTable {

    private final short[] codePoints;

    private final int[] lineNumbers;

    public LineNumberTable(short[] codePoints, int[] lineNumbers) {
        this.lineNumbers = lineNumbers.clone();
        this.codePoints = codePoints.clone();
    }

    public int getLineNumber(int codePoint) {
        short[] _codePoints = codePoints;

        int bottom = 0;
        int top = _codePoints.length;
        int current = (top >> 1);

        while ((top - bottom) > 1) {
            int cp = Conversions.unsigned(_codePoints[current]);
            if (codePoint >= cp) {
                bottom = current;
            } else {
                top = current;
            }
            current = (top + bottom) >> 1;
        }

        return lineNumbers[current];
    }
}
