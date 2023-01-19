package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public final class LineMap {

    private final int[] lineStartPoints;

    LineMap(String content) {
        lineStartPoints = buildLineMap(content);
    }

    private int[] buildLineMap(String content) {
        try (SourceReader reader = SourceReader.of(content)) {
            IntList lineStartPoints = new IntArrayList(reader.length() - reader.cursor());

            // Первая линия
            lineStartPoints.add(0);

            while (reader.hasMore()) {
                int ch = reader.readChar();
                if (ch == '\n') {
                    lineStartPoints.add(reader.cursor() - 1);
                }
            }
            lineStartPoints.add(reader.length() - 1);

            return lineStartPoints.toIntArray();
        }
    }

    public int getLineNumber(int pos) {
        int index = findLineIndex(pos);
        return index + 1;
    }

    public int getColumnNumber(int pos) {
        int index = findLineIndex(pos);
        return pos - lineStartPoints[index];
    }

    public int getLineStart(int pos) {
        return lineStartPoints[findLineIndex(pos)];
    }

    public int getLineEnd(int pos) {
        return lineStartPoints[findLineIndex(pos) + 1];
    }

    private int findLineIndex(int pos) {
        int bottom = 0;
        int top = lineStartPoints.length;
        int current = top >> 1;

        while ((top - bottom) > 1) {
           if (lineStartPoints[current] < pos) {
               bottom = current;
           } else {
               top = current;
           }
           current = (bottom + top) >> 1;
        }

        return current;
    }
}
