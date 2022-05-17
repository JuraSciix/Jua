package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Заметка: сделано на скорую руку.
 */
public class LineMap {

    private int[] lineMap;

    public LineMap(String content) {
        this(content.toCharArray());
    }

    public LineMap(char[] content) {
        if (content == null) {
            throw new NullPointerException("content");
        }
        IntArrayList lineMap = new IntArrayList();
        lineMap.add(0);
        for (int i = 0; i < content.length; i++) {
            if (content[i] == '\n') {
                lineMap.add(i + 1);
            }
        }
        this.lineMap = lineMap.toIntArray();
    }

    public int getLineNumber(int pos) {
        int[] _startPositions = lineMap;

        int f = 0;                      // from
        int t = _startPositions.length; // to
        int c = (t >> 1);               // center

        while ((t - f) > 1) {
            int sp = _startPositions[c];
            if (pos >= sp) {
                f = c;
            } else {
                t = c;
            }
            c = (t + f) >> 1;
        }

        return c + 1;
    }

    public int getOffsetNumber(int pos) {
        int linenum = getLineNumber(pos) - 1;
        return pos - lineMap[linenum];
    }
}
