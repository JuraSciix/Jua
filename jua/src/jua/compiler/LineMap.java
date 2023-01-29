package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import jua.utils.StringUtils;

public final class LineMap {

    private final int[] lineStartPoints;

    private final int contentEndPoint;

    LineMap(String content) {
        String rtrimmedContent = StringUtils.rtrim(content);
        lineStartPoints = buildLineMap(rtrimmedContent);
        contentEndPoint = rtrimmedContent.length();
    }

    private int[] buildLineMap(String content) {
        try (SourceReader reader = SourceReader.of(content)) {
            IntList lineStartPoints = new IntArrayList(reader.length() - reader.cursor());

            // Первая линия
            lineStartPoints.add(0);

            while (reader.hasMore()) {
                int ch = reader.readChar();
                if (ch == '\n') {
                    lineStartPoints.add(reader.cursor());
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

    public int getLineStart(int lineNum) {
        return lineStartPoints[lineNum - 1];
    }

    public int getLineEnd(int lineNum) {
        if (lineNum >= lineStartPoints.length)
            return contentEndPoint;
        return lineStartPoints[lineNum] - 1;
    }

    private int findLineIndex(int pos) {
        int bottom = 0;
        int top = lineStartPoints.length;
        int current = top >> 1;

        while ((top - bottom) > 1) {
           if (lineStartPoints[current] <= pos) {
               bottom = current;
           } else {
               top = current;
           }
           current = (bottom + top) >> 1;
        }

        return current;
    }
}
