package jua.compiler;

public class LNT {

    public final short[] codePoints;
    public final int[] lineNumbers;

    public LNT(short[] codePoints, int[] lineNumbers) {
        this.codePoints = codePoints;
        this.lineNumbers = lineNumbers;
    }

    public int getLineNumber(int codePoint) {
        short[] _codePoints = codePoints;

        int bottom = 0;
        int top = _codePoints.length;
        int current = (top >> 1);

        while ((top - bottom) > 1) {
            int cp = _codePoints[current] & 0xffff;
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
