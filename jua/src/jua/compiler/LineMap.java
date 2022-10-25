package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.IOException;

public final class LineMap {

    private final int[] lineStartPoints;

    public LineMap(Source source) throws IOException {
        lineStartPoints = buildLineMap(source);
    }

    private int[] buildLineMap(Source source) throws IOException {
        try (SourceReader reader = source.createReader()) {
            IntList lineStartPoints = new IntArrayList(reader.length() - reader.cursor());

            // Первая линия
            lineStartPoints.add(0);

            while (reader.hasMore()) {
                int ch = reader.readChar();
                if (ch == '\n') {
                    lineStartPoints.add(reader.cursor());
                }
            }
            lineStartPoints.add(reader.length());

            return lineStartPoints.toIntArray();
        } catch (Exception e) {
            throw new IOException(e);
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
