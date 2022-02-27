package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Заметка: сделано на скорую руку.
 */
public class LineMap {

    private int[] lineMap;

    public LineMap(String content) {
        if (content == null) {
            throw new NullPointerException("content");
        }
        IntArrayList lineMap = new IntArrayList();
        lineMap.add(0);
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                lineMap.add(i + 1);
            }
        }
        this.lineMap = lineMap.toIntArray();
    }

    public int getLineNumber(int pos) {
        int l = 0;
        int r = lineMap.length-1;

        int minIndex = 1;

        while (l <= r) {
            int c = (l+r)>>>1;

            if (lineMap[c] == pos) {
                return c + 1;
            } else if (lineMap[c] < pos) {
                minIndex = c;
                l = c + 1;
            } else {
                r = c - 1;
            }
        }

        return minIndex+1;
    }

    public int getOffsetNumber(int pos) {
        int linenum = getLineNumber(pos) - 1;
        return pos - lineMap[linenum];
    }
}
