package jua.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.IOException;

public final class LineMap {

    private final int[] lineStartPoints;

    public LineMap(Source source) throws IOException {
        lineStartPoints = buildLineMap(source);
    }

    private int[] buildLineMap(Source source) throws IOException {
        try (BufferReader reader = source.createReader()) {
            IntList lineStartPoints = new IntArrayList(reader.available());

            while (reader.unreadAvailable()) {
                int ch = reader.readChar();
                if (ch == '\n') {
                    lineStartPoints.add(reader.position());
                }
            }

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
